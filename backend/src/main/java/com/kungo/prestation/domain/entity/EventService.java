package com.kungo.prestation.domain.entity;

import com.kungo.category.Category;
import com.kungo.common.audit.BaseEntity;
import com.kungo.common.domain.Money;
import com.kungo.prestation.domain.enums.PriceType;
import com.kungo.prestation.domain.enums.ServiceStatus;
import com.kungo.professional.domain.entity.ProfessionalProfile;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Prestation événementielle proposée par un professionnel (ex: "Animation DJ mariage
 * 4h"). Nommée {@code EventService} pour éviter toute collision avec la couche
 * applicative "service" (voir package {@code com.kungo.prestation.service}).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"professional", "category"})
@Entity
@Table(name = "services", indexes = {
        @Index(name = "idx_services_professional", columnList = "professional_id"),
        @Index(name = "idx_services_category", columnList = "category_id"),
        @Index(name = "idx_services_status", columnList = "status")
})
public class EventService extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id", nullable = false)
    private ProfessionalProfile professional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 3000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 20)
    private PriceType priceType;

    @Embedded
    private Money price;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "conditions", length = 2000)
    private String conditions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.DRAFT;

    // ---- Comportement métier (machine à états) ----

    public void publish() {
        this.status = ServiceStatus.PUBLISHED;
    }

    public void pause() {
        this.status = ServiceStatus.PAUSED;
    }

    public void archive() {
        this.status = ServiceStatus.ARCHIVED;
    }

    public boolean isPublished() {
        return this.status == ServiceStatus.PUBLISHED;
    }
}
