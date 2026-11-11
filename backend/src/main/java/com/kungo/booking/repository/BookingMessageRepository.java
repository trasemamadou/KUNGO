package com.kungo.booking.repository;

import com.kungo.booking.entity.BookingMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingMessageRepository extends JpaRepository<BookingMessage, UUID> {

    List<BookingMessage> findByBookingIdOrderByCreatedAtAsc(UUID bookingId);

    long countByBookingIdAndSenderIdNotAndReadFalse(UUID bookingId, UUID senderId);

    @Modifying
    @Query("UPDATE BookingMessage m SET m.read = true "
            + "WHERE m.booking.id = :bookingId AND m.sender.id <> :recipientId AND m.read = false")
    void markAllReadForRecipient(@Param("bookingId") UUID bookingId, @Param("recipientId") UUID recipientId);
}
