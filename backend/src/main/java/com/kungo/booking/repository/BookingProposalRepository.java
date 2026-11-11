package com.kungo.booking.repository;

import com.kungo.booking.entity.BookingProposal;
import com.kungo.booking.enums.ProposalStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingProposalRepository extends JpaRepository<BookingProposal, UUID> {

    List<BookingProposal> findByBookingIdOrderByCreatedAtAsc(UUID bookingId);

    Optional<BookingProposal> findFirstByBookingIdAndStatusOrderByCreatedAtDesc(UUID bookingId, ProposalStatus status);
}
