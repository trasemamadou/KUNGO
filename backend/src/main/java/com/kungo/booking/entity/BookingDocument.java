package com.kungo.booking.entity;

import com.kungo.booking.enums.BookingDocumentType;
import com.kungo.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Document généré automatiquement au fil de la chaîne de réservation : contrat
 * (à l'acceptation) et reçu (au versement du paiement au professionnel). La
 * facture (paiement) reste portée par {@code payment.domain.entity.Invoice}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "booking")
@Entity
@Table(name = "booking_documents")
public class BookingDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private BookingDocumentType type;

    @Column(name = "document_number", nullable = false, unique = true, length = 50)
    private String documentNumber;

    @Column(name = "pdf_url", nullable = false)
    private String pdfUrl;

    @Builder.Default
    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt = Instant.now();
}
