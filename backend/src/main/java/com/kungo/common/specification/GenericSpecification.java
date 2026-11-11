package com.kungo.common.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduit un {@link SearchCriteria} unitaire en {@link Predicate} JPA Criteria API.
 * Supporte les champs imbriqués via notation pointée ("professional.address.city")
 * en construisant les jointures nécessaires à la volée.
 *
 * @param <T> type d'entité racine de la recherche
 */
@RequiredArgsConstructor
public class GenericSpecification<T> implements Specification<T> {

    private final SearchCriteria criteria;

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        Path<Object> path = resolvePath(root, criteria.getKey());

        return switch (criteria.getOperator()) {
            case EQUALS -> cb.equal(path, criteria.getValue());
            case NOT_EQUALS -> cb.notEqual(path, criteria.getValue());
            case LIKE -> cb.like(cb.lower(path.as(String.class)), "%" + String.valueOf(criteria.getValue()).toLowerCase() + "%");
            case STARTS_WITH -> cb.like(cb.lower(path.as(String.class)), String.valueOf(criteria.getValue()).toLowerCase() + "%");
            case ENDS_WITH -> cb.like(cb.lower(path.as(String.class)), "%" + String.valueOf(criteria.getValue()).toLowerCase());
            case GREATER_THAN -> cb.greaterThan((Path<Comparable>) (Path) path, (Comparable) criteria.getValue());
            case GREATER_THAN_OR_EQUAL -> cb.greaterThanOrEqualTo((Path<Comparable>) (Path) path, (Comparable) criteria.getValue());
            case LESS_THAN -> cb.lessThan((Path<Comparable>) (Path) path, (Comparable) criteria.getValue());
            case LESS_THAN_OR_EQUAL -> cb.lessThanOrEqualTo((Path<Comparable>) (Path) path, (Comparable) criteria.getValue());
            case BETWEEN -> cb.between((Path<Comparable>) (Path) path, (Comparable) criteria.getValue(), (Comparable) criteria.getValueTo());
            case IN -> path.in((List<?>) criteria.getValue());
            case NOT_IN -> cb.not(path.in((List<?>) criteria.getValue()));
            case IS_NULL -> cb.isNull(path);
            case IS_NOT_NULL -> cb.isNotNull(path);
            case IS_TRUE -> cb.isTrue(path.as(Boolean.class));
            case IS_FALSE -> cb.isFalse(path.as(Boolean.class));
        };
    }

    /**
     * Résout un chemin potentiellement imbriqué ("a.b.c") en composant des
     * {@code root.get(...)} successifs, ce qui génère automatiquement les jointures
     * implicites nécessaires (inner join par défaut, suffisant pour de la recherche).
     */
    private Path<Object> resolvePath(Root<T> root, String key) {
        String[] parts = key.split("\\.");
        Path<Object> path = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            path = path.get(parts[i]);
        }
        return path;
    }
}
