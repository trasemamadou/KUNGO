package com.kungo.common.specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * Parse des filtres génériques transmis en query param sous la forme
 * {@code field:operator:value}, séparés par des virgules, ex:
 * <pre>
 *   GET /api/v1/admin/users?filter=status:EQUALS:ACTIVE,createdAt:GREATER_THAN:2025-01-01T00:00:00Z
 * </pre>
 * Utilisé par les écrans d'administration pour une recherche dynamique multicritère
 * sans devoir créer un DTO de recherche dédié par entité.
 */
public final class SearchCriteriaParser {

    private static final String CRITERIA_SEPARATOR = ",";
    private static final String FIELD_SEPARATOR = ":";

    private SearchCriteriaParser() {
    }

    public static List<SearchCriteria> parse(String rawFilter) {
        List<SearchCriteria> result = new ArrayList<>();
        if (!StringUtils.hasText(rawFilter)) {
            return result;
        }
        for (String token : rawFilter.split(CRITERIA_SEPARATOR)) {
            String[] parts = token.split(FIELD_SEPARATOR, 3);
            if (parts.length < 2) {
                continue;
            }
            String key = parts[0].trim();
            SearchOperator operator = SearchOperator.valueOf(parts[1].trim().toUpperCase());
            if (operator == SearchOperator.IS_NULL || operator == SearchOperator.IS_NOT_NULL
                    || operator == SearchOperator.IS_TRUE || operator == SearchOperator.IS_FALSE) {
                result.add(SearchCriteria.builder().key(key).operator(operator).value(Boolean.TRUE).build());
                continue;
            }
            if (parts.length < 3) {
                continue;
            }
            if (operator == SearchOperator.BETWEEN) {
                String[] range = parts[2].split("\\|", 2);
                result.add(SearchCriteria.builder().key(key).operator(operator)
                        .value(range[0]).valueTo(range.length > 1 ? range[1] : null).build());
            } else if (operator == SearchOperator.IN || operator == SearchOperator.NOT_IN) {
                result.add(SearchCriteria.builder().key(key).operator(operator)
                        .value(Arrays.asList(parts[2].split("\\|"))).build());
            } else {
                result.add(SearchCriteria.builder().key(key).operator(operator).value(parts[2]).build());
            }
        }
        return result;
    }
}
