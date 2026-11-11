package com.kungo.prestation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Recherche marketplace grand public — combine tous les critères demandés :
 * catégorie, ville, fourchette de prix, note minimale, langue, expérience minimale,
 * disponibilité à une date donnée, recherche plein texte. Traduit en
 * {@link org.springframework.data.jpa.domain.Specification} par
 * {@code ServiceSpecifications}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSearchRequest {
    private String keyword;
    private UUID categoryId;
    private String city;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private String languageCode;
    private Integer minExperience;
    private LocalDate availableOn;
    private Boolean verifiedOnly;
}
