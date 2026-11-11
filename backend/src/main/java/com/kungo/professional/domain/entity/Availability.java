package com.kungo.professional.domain.entity;

import com.kungo.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Créneau de disponibilité d'un professionnel : soit récurrent (jour de semaine),
 * soit ponctuel (date précise) — jamais les deux à la fois. {@code blocked} permet
 * de fermer manuellement un créneau autrement disponible (congé, indisponibilité
 * exceptionnelle) sans supprimer la règle récurrente sous-jacente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "professional")
@Entity
@Table(name = "availabilities", indexes = {
        @Index(name = "idx_availabilities_professional", columnList = "professional_id"),
        @Index(name = "idx_availabilities_date", columnList = "specific_date")
})
public class Availability extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private ProfessionalProfile professional;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "recurring", nullable = false)
    @Builder.Default
    private boolean recurring = false;

    @Column(name = "blocked", nullable = false)
    @Builder.Default
    private boolean blocked = false;

    public boolean overlaps(Availability other) {
        boolean sameDay = this.recurring
                ? this.dayOfWeek == other.dayOfWeek
                : this.specificDate != null && this.specificDate.equals(other.specificDate);
        if (!sameDay) {
            return false;
        }
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }
}
