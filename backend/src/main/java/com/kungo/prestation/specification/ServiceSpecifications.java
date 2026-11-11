package com.kungo.prestation.specification;

import com.kungo.common.specification.GenericSpecificationBuilder;
import com.kungo.common.specification.SearchOperator;
import com.kungo.prestation.domain.entity.EventService;
import com.kungo.prestation.domain.enums.ServiceStatus;
import com.kungo.prestation.dto.request.ServiceSearchRequest;
import com.kungo.professional.domain.entity.Availability;
import com.kungo.professional.domain.entity.Language;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.professional.domain.enums.VerificationStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

/**
 * Traduit le formulaire de recherche marketplace ({@link ServiceSearchRequest}) en
 * {@link Specification} JPA. Les critères simples passent par le moteur générique
 * commun ; les jointures (langue) et la disponibilité (sous-requête corrélée) sont
 * construites manuellement, la Criteria API ne permettant pas de les exprimer via
 * une simple notation pointée sur des collections.
 */
public final class ServiceSpecifications {

    private ServiceSpecifications() {
    }

    public static Specification<EventService> from(ServiceSearchRequest request) {
        GenericSpecificationBuilder<EventService> builder = new GenericSpecificationBuilder<>();

        builder.with("status", SearchOperator.EQUALS, ServiceStatus.PUBLISHED);

        if (request.getCategoryId() != null) {
            builder.with("category.id", SearchOperator.EQUALS, request.getCategoryId());
        }
        if (StringUtils.hasText(request.getCity())) {
            builder.with("professional.address.city", SearchOperator.LIKE, request.getCity());
        }
        if (request.getMinPrice() != null && request.getMaxPrice() != null) {
            builder.with("price.amount", SearchOperator.BETWEEN, request.getMinPrice(), request.getMaxPrice());
        } else if (request.getMinPrice() != null) {
            builder.with("price.amount", SearchOperator.GREATER_THAN_OR_EQUAL, request.getMinPrice());
        } else if (request.getMaxPrice() != null) {
            builder.with("price.amount", SearchOperator.LESS_THAN_OR_EQUAL, request.getMaxPrice());
        }
        if (request.getMinRating() != null) {
            builder.with("professional.averageRating", SearchOperator.GREATER_THAN_OR_EQUAL, request.getMinRating());
        }
        if (request.getMinExperience() != null) {
            builder.with("professional.yearsExperience", SearchOperator.GREATER_THAN_OR_EQUAL, request.getMinExperience());
        }
        if (Boolean.TRUE.equals(request.getVerifiedOnly())) {
            builder.with("professional.verificationStatus", SearchOperator.EQUALS, VerificationStatus.VERIFIED);
        }
        if (StringUtils.hasText(request.getKeyword())) {
            builder.withFullText(request.getKeyword(), "title", "description");
        }

        Specification<EventService> spec = builder.build();

        if (StringUtils.hasText(request.getLanguageCode())) {
            spec = spec.and(hasLanguage(request.getLanguageCode()));
        }
        if (request.getAvailableOn() != null) {
            spec = spec.and(isAvailableOn(request.getAvailableOn()));
        }

        return spec;
    }

    private static Specification<EventService> hasLanguage(String languageCode) {
        return (root, query, cb) -> {
            Join<EventService, ProfessionalProfile> professionalJoin = root.join("professional", JoinType.INNER);
            Join<ProfessionalProfile, Language> languageJoin = professionalJoin.join("languages", JoinType.INNER);
            query.distinct(true);
            return cb.equal(cb.lower(languageJoin.get("code")), languageCode.toLowerCase());
        };
    }

    private static Specification<EventService> isAvailableOn(java.time.LocalDate date) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            var availabilityRoot = subquery.from(Availability.class);
            subquery.select(cb.literal(1L));

            Predicate sameProfessional = cb.equal(availabilityRoot.get("professional"), root.get("professional"));
            Predicate notBlocked = cb.isFalse(availabilityRoot.get("blocked"));
            Predicate recurringMatch = cb.and(
                    cb.isTrue(availabilityRoot.get("recurring")),
                    cb.equal(availabilityRoot.get("dayOfWeek"), date.getDayOfWeek()));
            Predicate specificMatch = cb.and(
                    cb.isFalse(availabilityRoot.get("recurring")),
                    cb.equal(availabilityRoot.get("specificDate"), date));

            subquery.where(cb.and(sameProfessional, notBlocked, cb.or(recurringMatch, specificMatch)));

            return cb.exists(subquery);
        };
    }
}
