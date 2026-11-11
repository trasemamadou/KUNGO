package com.kungo.professional.specification;

import com.kungo.common.specification.GenericSpecificationBuilder;
import com.kungo.common.specification.SearchOperator;
import com.kungo.professional.domain.entity.Language;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.domain.enums.VerificationStatus;
import com.kungo.professional.dto.request.ProfessionalSearchRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ProfessionalSpecifications {

    private ProfessionalSpecifications() {
    }

    public static Specification<ProfessionalProfile> from(ProfessionalSearchRequest request) {
        GenericSpecificationBuilder<ProfessionalProfile> builder = new GenericSpecificationBuilder<>();

        if (StringUtils.hasText(request.getCity())) {
            builder.with("address.city", SearchOperator.LIKE, request.getCity());
        }
        if (StringUtils.hasText(request.getRegion())) {
            builder.with("address.region", SearchOperator.LIKE, request.getRegion());
        }
        if (StringUtils.hasText(request.getCountry())) {
            builder.with("address.country", SearchOperator.LIKE, request.getCountry());
        }
        if (request.getMinRating() != null) {
            builder.with("averageRating", SearchOperator.GREATER_THAN_OR_EQUAL, request.getMinRating());
        }
        if (request.getMinExperience() != null) {
            builder.with("yearsExperience", SearchOperator.GREATER_THAN_OR_EQUAL, request.getMinExperience());
        }
        if (Boolean.TRUE.equals(request.getVerifiedOnly())) {
            builder.with("verificationStatus", SearchOperator.EQUALS, VerificationStatus.VERIFIED);
        }
        if (request.getVerificationStatus() != null) {
            builder.with("verificationStatus", SearchOperator.EQUALS, request.getVerificationStatus());
        }
        if (Boolean.TRUE.equals(request.getPremiumOnly())) {
            builder.with("premium", SearchOperator.IS_TRUE, true);
        }
        if (StringUtils.hasText(request.getKeyword())) {
            builder.withFullText(request.getKeyword(), "businessName", "biography");
        }

        Specification<ProfessionalProfile> spec = builder.build();

        if (StringUtils.hasText(request.getLanguageCode())) {
            spec = spec.and(hasLanguage(request.getLanguageCode()));
        }

        return spec;
    }

    /**
     * Filtre sur une association {@code @ManyToMany} : nécessite une jointure explicite
     * (le resolver de chemin générique ne gère que les attributs singuliers).
     */
    private static Specification<ProfessionalProfile> hasLanguage(String languageCode) {
        return (root, query, cb) -> {
            Join<ProfessionalProfile, Language> languageJoin = root.join("languages", JoinType.INNER);
            query.distinct(true);
            return cb.equal(cb.lower(languageJoin.get("code")), languageCode.toLowerCase());
        };
    }
}
