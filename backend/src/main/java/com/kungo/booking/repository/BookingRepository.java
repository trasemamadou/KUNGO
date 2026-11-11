package com.kungo.booking.repository;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.enums.BookingStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    List<Booking> findByClientId(UUID clientId);

    List<Booking> findByProfessionalId(UUID professionalId);

    @Query("SELECT b FROM Booking b WHERE b.status IN :statuses AND b.eventDate < :now")
    List<Booking> findByStatusInAndEventDateBefore(Collection<BookingStatus> statuses, LocalDateTime now);

    long countByStatus(BookingStatus status);

    long countByStatusIn(Collection<BookingStatus> statuses);
}
