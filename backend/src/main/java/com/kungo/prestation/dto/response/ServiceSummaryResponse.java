package com.kungo.prestation.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Vue allégée pour les cartes de résultats de recherche marketplace.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSummaryResponse {
    private UUID id;
    private String title;
    private BigDecimal price;
    private String currency;
    private String coverImageUrl;
    private String categoryName;
    private UUID professionalId;
    private String professionalName;
    private String professionalAvatarUrl;
    private String city;
    private BigDecimal professionalRating;
    private boolean professionalVerified;
}
