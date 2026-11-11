package com.kungo.payment.service.impl;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.entity.BookingStatusHistory;
import com.kungo.booking.enums.BookingStatus;
import com.kungo.booking.event.BookingStatusChangedEvent;
import com.kungo.booking.repository.BookingStatusHistoryRepository;
import com.kungo.booking.service.BookingService;
import com.kungo.common.domain.Money;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.payment.domain.entity.Payout;
import com.kungo.payment.dto.response.PayoutResponse;
import com.kungo.payment.mapper.PaymentMapper;
import com.kungo.payment.repository.PayoutRepository;
import com.kungo.payment.service.PayoutService;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.service.ProfessionalService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PayoutServiceImpl implements PayoutService {

    private final PayoutRepository payoutRepository;
    private final ProfessionalService professionalService;
    private final BookingService bookingService;
    private final BookingStatusHistoryRepository bookingStatusHistoryRepository;
    private final PaymentMapper paymentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<PayoutResponse> listForProfessional(UUID professionalId) {
        ProfessionalProfile professional = professionalService.getEntityById(professionalId);
        SecurityUtils.requireOwnerOrRole(professional.getUser().getId(), "ADMIN", "SUPER_ADMIN");
        return payoutRepository.findByProfessionalId(professionalId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PayoutResponse create(UUID professionalId, BigDecimal amount, String currency) {
        ProfessionalProfile professional = professionalService.getEntityById(professionalId);
        Payout payout = Payout.builder()
                .professional(professional)
                .amount(Money.of(amount, currency))
                .build();
        Payout saved = payoutRepository.save(payout);
        log.info("Payout {} created for professional {}: {} {}", saved.getId(), professionalId, amount, currency);
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PayoutResponse markPaid(UUID payoutId, String reference) {
        Payout payout = getEntity(payoutId);
        payout.markPaid(reference);
        return paymentMapper.toResponse(payoutRepository.save(payout));
    }

    @Override
    @Transactional
    public PayoutResponse markFailed(UUID payoutId) {
        Payout payout = getEntity(payoutId);
        payout.markFailed();
        return paymentMapper.toResponse(payoutRepository.save(payout));
    }

    @Override
    // REQUIRES_NEW : appelé depuis un @TransactionalEventListener(phase = AFTER_COMMIT),
    // donc après la fin de la transaction d'origine. Une propagation REQUIRED risquerait
    // de rattacher cet appel à des ressources JPA déjà en cours de nettoyage (OSIV) côté
    // thread de la requête d'origine, avec pour effet un commit qui n'écrit jamais en base
    // (observé en test manuel : le log de succès s'affiche mais aucune ligne n'est persistée).
    // REQUIRES_NEW garantit une connexion et un commit totalement indépendants.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void settleForCompletedBooking(UUID bookingId) {
        if (payoutRepository.existsByBookingId(bookingId)) {
            log.debug("Payout already settled for booking {}, skipping", bookingId);
            return;
        }

        Booking booking = bookingService.getEntityById(bookingId);
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            log.warn("Cannot settle payout for booking {}: status is {}, not COMPLETED", bookingId, booking.getStatus());
            return;
        }
        if (booking.getProfessionalAmount() == null || booking.getTotalPrice() == null) {
            log.warn("Cannot settle payout for booking {}: no professional amount recorded", bookingId);
            return;
        }

        Payout payout = Payout.builder()
                .professional(booking.getProfessional())
                .booking(booking)
                .amount(Money.of(booking.getProfessionalAmount(), booking.getTotalPrice().getCurrency()))
                .build();
        Payout saved = payoutRepository.save(payout);
        saved.markPaid("AUTO-" + booking.getId());
        payoutRepository.save(saved);

        BookingStatus previous = booking.getStatus();
        booking.markPayoutSettled();
        bookingStatusHistoryRepository.save(BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(previous)
                .toStatus(booking.getStatus())
                .comment("Versement automatique au professionnel")
                .build());
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking.getId(), previous, booking.getStatus()));

        log.info("Payout {} automatically settled for booking {}: {} {}",
                saved.getId(), bookingId, saved.getAmount().getAmount(), saved.getAmount().getCurrency());
    }

    private Payout getEntity(UUID id) {
        return payoutRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("error.not-found", id));
    }
}
