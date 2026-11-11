package com.kungo.common.specification;

/**
 * Opérateurs supportés par le moteur de recherche dynamique générique.
 * Utilisés pour construire des {@link org.springframework.data.jpa.domain.Specification}
 * à partir de critères transmis par le frontend (query params ou corps de requête).
 */
public enum SearchOperator {
    EQUALS,
    NOT_EQUALS,
    LIKE,
    STARTS_WITH,
    ENDS_WITH,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    BETWEEN,
    IN,
    NOT_IN,
    IS_NULL,
    IS_NOT_NULL,
    IS_TRUE,
    IS_FALSE
}
