package com.kungo.common.specification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Représente un critère de filtre unitaire : {@code champ operateur valeur}.
 * Le {@code key} supporte la notation pointée pour les jointures/champs imbriqués,
 * ex: {@code "professional.address.city"} ou {@code "category.slug"}.
 * {@code valueTo} n'est utilisé que pour l'opérateur {@link SearchOperator#BETWEEN}.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchCriteria {
    private String key;
    private SearchOperator operator;
    private Object value;
    private Object valueTo;
}
