package com.kungo.booking.service;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.entity.BookingMessage;
import com.kungo.booking.dto.BookingMessageResponse;
import com.kungo.booking.event.BookingMessageSentEvent;
import com.kungo.booking.mapper.BookingMapper;
import com.kungo.booking.repository.BookingMessageRepository;
import com.kungo.booking.repository.BookingRepository;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ForbiddenException;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.common.util.FileStorageService;
import com.kungo.user.domain.entity.User;
import com.kungo.user.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingMessageServiceImpl implements BookingMessageService {

    private static final String MESSAGE_ATTACHMENT_FOLDER = "booking-messages";

    private final BookingMessageRepository messageRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public List<BookingMessageResponse> list(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        UUID currentUserId = requireParty(booking);

        messageRepository.markAllReadForRecipient(bookingId, currentUserId);
        return messageRepository.findByBookingIdOrderByCreatedAtAsc(bookingId).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookingMessageResponse send(UUID bookingId, String content, MultipartFile file) {
        Booking booking = getBooking(bookingId);
        UUID currentUserId = requireParty(booking);

        boolean hasContent = content != null && !content.isBlank();
        boolean hasFile = file != null && !file.isEmpty();
        if (!hasContent && !hasFile) {
            throw new BadRequestException("error.validation");
        }

        User sender = userService.getEntityById(currentUserId);
        BookingMessage.BookingMessageBuilder builder = BookingMessage.builder()
                .booking(booking)
                .sender(sender)
                .content(hasContent ? content : null);

        if (hasFile) {
            builder.attachmentUrl(fileStorageService.store(file, MESSAGE_ATTACHMENT_FOLDER))
                    .attachmentName(file.getOriginalFilename());
        }

        BookingMessage saved = messageRepository.save(builder.build());
        eventPublisher.publishEvent(new BookingMessageSentEvent(bookingId, saved.getId(), currentUserId));
        log.info("Message {} sent in booking {} by {}", saved.getId(), bookingId, currentUserId);
        return bookingMapper.toResponse(saved);
    }

    @Override
    public long unreadCount(UUID bookingId) {
        Booking booking = getBooking(bookingId);
        UUID currentUserId = requireParty(booking);
        return messageRepository.countByBookingIdAndSenderIdNotAndReadFalse(bookingId, currentUserId);
    }

    private Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("booking.not-found", bookingId));
    }

    private UUID requireParty(Booking booking) {
        UUID currentUserId = SecurityUtils.currentUserId();
        boolean isParty = booking.getClient().getId().equals(currentUserId)
                || booking.getProfessional().getUser().getId().equals(currentUserId);
        if (!isParty && !SecurityUtils.hasRole("ADMIN") && !SecurityUtils.hasRole("SUPER_ADMIN")) {
            throw new ForbiddenException("error.forbidden");
        }
        return currentUserId;
    }
}
