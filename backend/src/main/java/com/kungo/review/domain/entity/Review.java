package com.kungo.review.domain.entity;

import com.kungo.booking.entity.Booking;
import com.kungo.common.audit.BaseEntity;
import com.kungo.review.domain.enums.ModerationStatus;
import com.kungo.user.domain.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Avis bidirectionnel : le client note le professionnel et vice-versa, chacun sur
 * la même réservation ({@code booking}) mais avec un {@code author}/{@code target}
 * distincts — deux lignes {@code Review} possibles par réservation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"booking", "author", "target"})
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_reviews_booking", columnList = "booking_id"),
        @Index(name = "idx_reviews_target", columnList = "target_id")
})
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    /** Moyenne des 5 critères ci-dessous, calculée à la création de l'avis. */
    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "punctuality", nullable = false)
    private int punctuality;

    @Column(name = "quality", nullable = false)
    private int quality;

    @Column(name = "communication", nullable = false)
    private int communication;

    @Column(name = "value_for_money", nullable = false)
    private int valueForMoney;

    @Column(name = "professionalism", nullable = false)
    private int professionalism;

    @Column(name = "comment", length = 2000)
    private String comment;

    @Column(name = "professional_response", length = 2000)
    private String professionalResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false, length = 20)
    @Builder.Default
    private ModerationStatus moderationStatus = ModerationStatus.APPROVED;

    public void respond(String response) {
        this.professionalResponse = response;
    }

    public void approve() {
        this.moderationStatus = ModerationStatus.APPROVED;
    }

    public void reject() {
        this.moderationStatus = ModerationStatus.REJECTED;
    }
}
