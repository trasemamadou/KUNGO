package com.kungo.payment.repository;

import com.kungo.payment.domain.entity.Payment;
import com.kungo.payment.domain.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByBookingId(UUID bookingId);

    Optional<Payment> findByTransactionRef(String transactionRef);

    @Query("SELECT COALESCE(SUM(p.amount.amount), 0) FROM Payment p WHERE p.status = :status")
    BigDecimal sumAmountByStatus(PaymentStatus status);
}
