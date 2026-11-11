package com.kungo.booking.repository;

import com.kungo.booking.entity.BookingDocument;
import com.kungo.booking.enums.BookingDocumentType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingDocumentRepository extends JpaRepository<BookingDocument, UUID> {

    List<BookingDocument> findByBookingIdOrderByGeneratedAtAsc(UUID bookingId);

    Optional<BookingDocument> findByBookingIdAndType(UUID bookingId, BookingDocumentType type);
}
