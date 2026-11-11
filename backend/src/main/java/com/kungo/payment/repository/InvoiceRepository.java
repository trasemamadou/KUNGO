package com.kungo.payment.repository;

import com.kungo.payment.domain.entity.Invoice;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByPaymentId(UUID paymentId);

    long countByIssuedAtBetween(java.time.Instant from, java.time.Instant to);
}
