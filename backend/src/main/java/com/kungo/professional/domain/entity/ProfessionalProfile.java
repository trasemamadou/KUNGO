package com.kungo.professional.domain.entity;

import com.kungo.common.audit.BaseEntity;
import com.kungo.common.domain.Address;
import com.kungo.professional.domain.enums.VerificationStatus;
import com.kungo.user.domain.entity.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Profil métier d'un professionnel de l'événementiel — extension 1-1 de {@link User}
 * pour les comptes de rôle PROFESSIONNEL. Racine d'agrégat pour les statistiques
 * agrégées (note moyenne, nombre de réservations) recalculées par les modules
 * {@code review} et {@code booking} via événements applicatifs (jamais par requête
 * cross-module directe, pour garder le couplage faible).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"user", "skills", "languages"})
@Entity
@Table(name = "professional_profiles", indexes = {
        @Index(name = "idx_professional_profiles_user", columnList = "user_id", unique = true),
        @Index(name = "idx_professional_profiles_verification", columnList = "verification_status")
})
public class ProfessionalProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "business_name", length = 150)
    private String businessName;

    @Column(name = "biography", length = 3000)
    private String biography;

    @Column(name = "years_experience")
    @Builder.Default
    private int yearsExperience = 0;

    @Embedded
    private Address address;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "professional_skills",
            joinColumns = @JoinColumn(name = "professional_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "professional_languages",
            joinColumns = @JoinColumn(name = "professional_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id"))
    @Builder.Default
    private Set<Language> languages = new HashSet<>();

    @Column(name = "verification_status", nullable = false, length = 20)
    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(name = "premium", nullable = false)
    @Builder.Default
    private boolean premium = false;

    @Column(name = "premium_expires_at")
    private Instant premiumExpiresAt;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private int totalReviews = 0;

    @Column(name = "total_bookings", nullable = false)
    @Builder.Default
    private int totalBookings = 0;

    @Column(name = "response_time_hours")
    private Integer responseTimeHours;

    // ---- Comportement métier ----

    public void submitForVerification() {
        this.verificationStatus = VerificationStatus.PENDING;
    }

    public void approveVerification() {
        this.verificationStatus = VerificationStatus.VERIFIED;
    }

    public void rejectVerification() {
        this.verificationStatus = VerificationStatus.REJECTED;
    }

    public boolean isVerified() {
        return this.verificationStatus == VerificationStatus.VERIFIED;
    }

    public void activatePremium(Instant expiresAt) {
        this.premium = true;
        this.premiumExpiresAt = expiresAt;
    }

    public void expirePremiumIfNeeded() {
        if (this.premium && this.premiumExpiresAt != null && this.premiumExpiresAt.isBefore(Instant.now())) {
            this.premium = false;
        }
    }

    public void registerNewReview(int rating) {
        BigDecimal totalScore = this.averageRating.multiply(BigDecimal.valueOf(this.totalReviews));
        this.totalReviews += 1;
        this.averageRating = totalScore.add(BigDecimal.valueOf(rating))
                .divide(BigDecimal.valueOf(this.totalReviews), 2, java.math.RoundingMode.HALF_UP);
    }

    public void incrementBookingCount() {
        this.totalBookings += 1;
    }
}
