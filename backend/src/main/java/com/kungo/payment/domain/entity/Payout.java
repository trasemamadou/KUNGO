package com.kungo.payment.domain.entity;

import com.kungo.booking.entity.Booking;
import com.kungo.common.audit.BaseEntity;
import com.kungo.common.domain.Money;
import com.kungo.payment.domain.enums.PayoutStatus;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
 * Reversement au professionnel du montant net (prix - commission) d'une ou
 * plusieurs réservations payées. Déclenché par un job planifié (T+2 jours après
 * la prestation) — voir {@code PayoutScheduler}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "professional")
@Entity
@Table(name = "payouts")
public class Payout extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private ProfessionalProfile professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Embedded
    private Money amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "reference")
    private String reference;

    @Column(name = "paid_at")
    private Instant paidAt;

    public void markPaid(String reference) {
        this.status = PayoutStatus.PAID;
        this.reference = reference;
        this.paidAt = Instant.now();
    }

    public void markFailed() {
        this.status = PayoutStatus.FAILED;
    }
}
