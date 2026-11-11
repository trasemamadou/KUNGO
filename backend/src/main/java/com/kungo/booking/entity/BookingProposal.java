package com.kungo.booking.entity;

import com.kungo.booking.enums.ProposalStatus;
import com.kungo.common.audit.BaseEntity;
import com.kungo.common.domain.Money;
import com.kungo.user.domain.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Contre-proposition dans une négociation de réservation : un des deux côtés
 * (client ou professionnel) peut proposer un nouveau prix, une nouvelle date,
 * une nouvelle durée ou de nouvelles conditions. L'autre partie accepte, rejette
 * ou soumet à son tour une nouvelle {@code BookingProposal} — la précédente passe
 * alors à {@link ProposalStatus#SUPERSEDED}. Seuls les champs modifiés par cette
 * proposition sont renseignés (les autres restent {@code null}, signifiant "pas
 * de changement par rapport à la valeur courante de la réservation").
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"booking", "proposedBy"})
@Entity
@Table(name = "booking_proposals", indexes = {
        @Index(name = "idx_booking_proposals_booking", columnList = "booking_id")
})
public class BookingProposal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposed_by", nullable = false)
    private User proposedBy;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "proposed_price_amount"))
    @AttributeOverride(name = "currency", column = @Column(name = "proposed_price_currency"))
    private Money proposedPrice;

    @Column(name = "proposed_event_date")
    private LocalDateTime proposedEventDate;

    @Column(name = "proposed_duration_minutes")
    private Integer proposedDurationMinutes;

    @Column(name = "proposed_conditions", length = 2000)
    private String proposedConditions;

    @Column(name = "message", length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.PENDING;

    public void accept() {
        this.status = ProposalStatus.ACCEPTED;
    }

    public void reject() {
        this.status = ProposalStatus.REJECTED;
    }

    public void supersede() {
        this.status = ProposalStatus.SUPERSEDED;
    }
}
