package com.kungo.payment.service.impl;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.entity.BookingStatusHistory;
import com.kungo.booking.enums.BookingStatus;
import com.kungo.booking.repository.BookingStatusHistoryRepository;
import com.kungo.booking.service.BookingService;
import com.kungo.common.domain.Money;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ForbiddenException;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.payment.domain.entity.Commission;
import com.kungo.payment.domain.entity.Payment;
import com.kungo.payment.domain.enums.PaymentStatus;
import com.kungo.payment.dto.request.PaymentRequest;
import com.kungo.payment.dto.response.PaymentResponse;
import com.kungo.payment.event.PaymentSucceededEvent;
import com.kungo.payment.mapper.PaymentMapper;
import com.kungo.payment.repository.CommissionRepository;
import com.kungo.payment.repository.PaymentRepository;
import com.kungo.payment.service.CommissionCalculator;
import com.kungo.payment.service.InvoiceService;
import com.kungo.payment.service.PaymentService;
import com.kungo.payment.service.strategy.PaymentChargeResult;
import com.kungo.payment.service.strategy.PaymentStrategy;
import com.kungo.payment.service.strategy.PaymentStrategyFactory;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.repository.ProfessionalProfileRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CommissionRepository commissionRepository;
    private final ProfessionalProfileRepository professionalProfileRepository;
    private final BookingService bookingService;
    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final CommissionCalculator commissionCalculator;
    private final InvoiceService invoiceService;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public PaymentResponse initiate(PaymentRequest request) {
        Booking booking = bookingService.getEntityById(request.getBookingId());
        SecurityUtils.requireOwnerOrRole(booking.getClient().getId(), "ADMIN", "SUPER_ADMIN");

        if (booking.getStatus() != BookingStatus.ACCEPTED) {
            throw new BadRequestException("booking.invalid-status-transition", booking.getStatus(), "PAID");
        }
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new BadRequestException("payment.failed");
        }

        PaymentStrategy strategy = paymentStrategyFactory.resolve(request.getProvider());
        PaymentChargeResult result = strategy.charge(booking.getId().toString(), booking.getTotalPrice());

        Payment payment = Payment.builder()
                .booking(booking)
                .provider(request.getProvider())
                .amount(booking.getTotalPrice())
                .status(result.status())
                .transactionRef(result.transactionRef())
                .providerClientSecret(result.clientSecret())
                .build();
        Payment saved = paymentRepository.save(payment);

        log.info("Payment {} initiated for booking {} via {}", saved.getId(), booking.getId(), request.getProvider());
        return paymentMapper.toResponse(saved);
    }

    @Override
    public PaymentResponse getByBooking(UUID bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("booking.not-found", bookingId));
        UUID currentUserId = SecurityUtils.currentUserId();
        boolean isParty = payment.getBooking().getClient().getId().equals(currentUserId)
                || payment.getBooking().getProfessional().getUser().getId().equals(currentUserId);
        if (!isParty && !SecurityUtils.hasRole("ADMIN") && !SecurityUtils.hasRole("SUPER_ADMIN")) {
            throw new ForbiddenException("error.forbidden");
        }
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void handleProviderSucceeded(String transactionRef) {
        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new BadRequestException("payment.failed"));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            return; // idempotence : webhook potentiellement livré plusieurs fois
        }

        Booking booking = payment.getBooking();
        Money commissionAmount = commissionCalculator.calculate(payment.getAmount(), booking.getService().getCategory());
        Money professionalAmount = payment.getAmount().subtract(commissionAmount);
        BigDecimal percentageApplied = commissionCalculator.resolvePercentage(booking.getService().getCategory());

        payment.markSucceeded(transactionRef);
        paymentRepository.save(payment);

        commissionRepository.save(Commission.builder()
                .payment(payment)
                .percentageApplied(percentageApplied)
                .amount(commissionAmount.getAmount())
                .build());

        // `booking` est une entité gérée par le contexte de persistance courant (chargée via
        // payment.getBooking()) : la transition est répercutée en base par dirty-checking JPA.
        BookingStatus previousStatus = booking.getStatus();
        booking.confirmPayment(commissionAmount.getAmount(), professionalAmount.getAmount());
        bookingStatusHistoryRepository.save(BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(previousStatus)
                .toStatus(booking.getStatus())
                .comment("Paiement confirmé via " + payment.getProvider())
                .build());

        ProfessionalProfile professional = booking.getProfessional();
        professional.incrementBookingCount();
        professionalProfileRepository.save(professional);

        var invoice = invoiceService.generate(payment, booking);
        payment.setInvoiceUrl(invoice.getPdfUrl());
        paymentRepository.save(payment);

        eventPublisher.publishEvent(new PaymentSucceededEvent(booking.getId(), payment.getId()));
        log.info("Payment {} succeeded for booking {}", payment.getId(), booking.getId());
    }

    @Override
    @Transactional
    public void handleProviderFailed(String transactionRef) {
        paymentRepository.findByTransactionRef(transactionRef).ifPresent(payment -> {
            payment.markFailed();
            paymentRepository.save(payment);
            log.warn("Payment {} failed (provider ref {})", payment.getId(), transactionRef);
        });
    }

    @Override
    @Transactional
    public void refund(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("payment.failed"));

        PaymentStrategy strategy = paymentStrategyFactory.resolve(payment.getProvider());
        strategy.refund(payment.getTransactionRef());

        payment.markRefunded();
        paymentRepository.save(payment);
        log.info("Payment {} refunded", paymentId);
    }
}
