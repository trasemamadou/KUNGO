package com.kungo.professional.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vue allégée pour les cartes de résultats de recherche / listes (favoris, etc.).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalSummaryResponse {
    private UUID id;
    private String fullName;
    private String avatarUrl;
    private String businessName;
    private String city;
    private String country;
    private BigDecimal averageRating;
    private int totalReviews;
    private boolean verified;
    private boolean premium;
}
