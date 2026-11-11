package com.kungo.payment.repository;

import com.kungo.payment.domain.entity.Payout;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PayoutRepository extends JpaRepository<Payout, UUID>, JpaSpecificationExecutor<Payout> {

    List<Payout> findByProfessionalId(UUID professionalId);

    boolean existsByBookingId(UUID bookingId);
}
