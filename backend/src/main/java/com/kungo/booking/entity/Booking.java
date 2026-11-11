package com.kungo.booking.entity;

import com.kungo.booking.enums.BookingStatus;
import com.kungo.common.audit.BaseEntity;
import com.kungo.common.domain.Money;
import com.kungo.common.exception.InvalidStateTransitionException;
import com.kungo.prestation.domain.entity.EventService;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.user.domain.entity.User;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Racine d'agrégat de la réservation — couvre tout le cycle de vie, de la demande
 * initiale à la négociation, au paiement, au suivi de la prestation et à la double
 * confirmation de fin. Toute transition de statut passe par une méthode métier
 * dédiée (jamais de setter public sur {@code status}) — voir {@link #transitionTo}.
 * L'historique des transitions est tracé séparément par {@link BookingStatusHistory},
 * et les échanges de négociation par {@link BookingProposal}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"client", "professional", "service"})
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_client", columnList = "client_id"),
        @Index(name = "idx_bookings_professional", columnList = "professional_id"),
        @Index(name = "idx_bookings_status", columnList = "status")
})
public class Booking extends BaseEntity {

    private static final Set<BookingStatus> POST_PAYMENT_ACTIVE = EnumSet.of(
            BookingStatus.PAID, BookingStatus.PREPARING, BookingStatus.EN_ROUTE,
            BookingStatus.ARRIVED, BookingStatus.IN_PROGRESS);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private ProfessionalProfile professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private EventService service;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "event_address", length = 500)
    private String eventAddress;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(name = "participant_count")
    private Integer participantCount;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "special_requests", length = 1000)
    private String specialRequests;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "professional_message", length = 1000)
    private String professionalMessage;

    @Embedded
    private Money totalPrice;

    @Column(name = "commission_amount", precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "professional_amount", precision = 12, scale = 2)
    private BigDecimal professionalAmount;

    @Column(name = "client_confirmed_at")
    private Instant clientConfirmedAt;

    @Column(name = "professional_confirmed_at")
    private Instant professionalConfirmedAt;

    // ---- Machine à états ----

    /** Accepte directement la demande initiale, sans passer par une négociation. */
    public void accept() {
        transitionTo(BookingStatus.ACCEPTED, EnumSet.of(BookingStatus.PENDING));
    }

    public void reject(String reason) {
        transitionTo(BookingStatus.REJECTED, EnumSet.of(BookingStatus.PENDING, BookingStatus.NEGOTIATING));
        this.professionalMessage = reason;
    }

    /** Ouvre (ou poursuit) une négociation : une contre-proposition vient d'être soumise. */
    public void enterNegotiation() {
        transitionTo(BookingStatus.NEGOTIATING, EnumSet.of(BookingStatus.PENDING, BookingStatus.NEGOTIATING));
    }

    /**
     * Applique les termes d'une {@link BookingProposal} acceptée par les deux parties
     * et clôt la négociation. Seuls les champs non-nuls de la proposition écrasent la
     * valeur courante.
     */
    public void acceptProposal(Money price, LocalDateTime eventDate, Integer durationMinutes, String conditions) {
        transitionTo(BookingStatus.ACCEPTED, EnumSet.of(BookingStatus.PENDING, BookingStatus.NEGOTIATING));
        if (price != null) {
            this.totalPrice = price;
        }
        if (eventDate != null) {
            this.eventDate = eventDate;
        }
        if (durationMinutes != null) {
            this.estimatedDurationMinutes = durationMinutes;
        }
        if (conditions != null) {
            this.specialRequests = conditions;
        }
    }

    public void confirmPayment(BigDecimal commission, BigDecimal professionalShare) {
        transitionTo(BookingStatus.PAID, EnumSet.of(BookingStatus.ACCEPTED));
        this.commissionAmount = commission;
        this.professionalAmount = professionalShare;
    }

    public void prepare() {
        transitionTo(BookingStatus.PREPARING, EnumSet.of(BookingStatus.PAID));
    }

    public void startTravel() {
        transitionTo(BookingStatus.EN_ROUTE, EnumSet.of(BookingStatus.PAID, BookingStatus.PREPARING));
    }

    public void markArrived() {
        transitionTo(BookingStatus.ARRIVED, EnumSet.of(BookingStatus.PREPARING, BookingStatus.EN_ROUTE));
    }

    public void start() {
        transitionTo(BookingStatus.IN_PROGRESS, EnumSet.of(
                BookingStatus.PAID, BookingStatus.PREPARING, BookingStatus.EN_ROUTE, BookingStatus.ARRIVED));
    }

    /**
     * Confirmation de fin de prestation côté client. La réservation ne passe à
     * {@link BookingStatus#COMPLETED} que lorsque les deux parties ont confirmé.
     */
    public boolean clientConfirmCompletion() {
        requireStatusIn(POST_PAYMENT_ACTIVE);
        this.clientConfirmedAt = Instant.now();
        return finalizeCompletionIfBothConfirmed();
    }

    public boolean professionalConfirmCompletion() {
        requireStatusIn(POST_PAYMENT_ACTIVE);
        this.professionalConfirmedAt = Instant.now();
        return finalizeCompletionIfBothConfirmed();
    }

    private boolean finalizeCompletionIfBothConfirmed() {
        if (this.clientConfirmedAt != null && this.professionalConfirmedAt != null) {
            this.status = BookingStatus.COMPLETED;
            return true;
        }
        return false;
    }

    public void markPayoutSettled() {
        transitionTo(BookingStatus.PAYOUT_SETTLED, EnumSet.of(BookingStatus.COMPLETED));
    }

    public void cancel(String reason) {
        transitionTo(BookingStatus.CANCELLED, EnumSet.of(
                BookingStatus.PENDING, BookingStatus.NEGOTIATING, BookingStatus.ACCEPTED, BookingStatus.PAID,
                BookingStatus.PREPARING, BookingStatus.EN_ROUTE, BookingStatus.ARRIVED));
        this.description = reason != null ? reason : this.description;
    }

    public boolean isActive() {
        return status != BookingStatus.COMPLETED && status != BookingStatus.PAYOUT_SETTLED
                && status != BookingStatus.CANCELLED && status != BookingStatus.REJECTED;
    }

    private void transitionTo(BookingStatus target, Set<BookingStatus> allowedFrom) {
        requireStatusIn(allowedFrom, target);
        this.status = target;
    }

    private void requireStatusIn(Set<BookingStatus> allowed) {
        requireStatusIn(allowed, this.status);
    }

    private void requireStatusIn(Set<BookingStatus> allowed, BookingStatus target) {
        if (!allowed.contains(this.status)) {
            throw new InvalidStateTransitionException("booking.invalid-status-transition", this.status, target);
        }
    }
}
