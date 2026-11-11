package com.kungo.professional.dto.request;

import com.kungo.professional.domain.enums.VerificationStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Filtres de recherche au niveau du profil professionnel (annuaire / back-office).
 * La recherche marketplace grand public combinant prix, catégorie et disponibilité
 * se fait au niveau du module {@code service} (voir {@code ServiceSearchRequest}),
 * qui joint {@code professional.*} pour les critères de réputation.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalSearchRequest {
    private String keyword;
    private String city;
    private String region;
    private String country;
    private String languageCode;
    private Boolean verifiedOnly;
    private Boolean premiumOnly;
    private BigDecimal minRating;
    private Integer minExperience;
    private VerificationStatus verificationStatus;
}
