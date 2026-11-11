package com.kungo.booking.repository;

import com.kungo.booking.entity.BookingStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, UUID> {

    List<BookingStatusHistory> findByBookingIdOrderByCreatedAtAsc(UUID bookingId);
}
