package com.kungo.booking.repository;

import com.kungo.booking.entity.BookingAttachment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingAttachmentRepository extends JpaRepository<BookingAttachment, UUID> {

    List<BookingAttachment> findByBookingIdOrderByCreatedAtAsc(UUID bookingId);
}
