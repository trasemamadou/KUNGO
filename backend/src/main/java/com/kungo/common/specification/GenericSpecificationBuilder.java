package com.kungo.common.specification;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Construit une {@link Specification} composite en combinant en AND une liste de
 * {@link SearchCriteria}, plus une éventuelle recherche plein texte en OR sur un
 * ensemble de champs (ex: titre + description + nom professionnel).
 *
 * Utilisation typique dans un service :
 * <pre>
 *   Specification&lt;EventService&gt; spec = new GenericSpecificationBuilder&lt;EventService&gt;()
 *       .with(criteriaList)
 *       .withFullText(keyword, "title", "description")
 *       .build();
 * </pre>
 */
public class GenericSpecificationBuilder<T> {

    private final List<SearchCriteria> criteriaList = new ArrayList<>();
    private String fullTextKeyword;
    private String[] fullTextFields = new String[0];

    public GenericSpecificationBuilder<T> with(String key, SearchOperator operator, Object value) {
        criteriaList.add(SearchCriteria.builder().key(key).operator(operator).value(value).build());
        return this;
    }

    public GenericSpecificationBuilder<T> with(String key, SearchOperator operator, Object value, Object valueTo) {
        criteriaList.add(SearchCriteria.builder().key(key).operator(operator).value(value).valueTo(valueTo).build());
        return this;
    }

    public GenericSpecificationBuilder<T> with(List<SearchCriteria> criteria) {
        if (criteria != null) {
            this.criteriaList.addAll(criteria);
        }
        return this;
    }

    public GenericSpecificationBuilder<T> withFullText(String keyword, String... fields) {
        this.fullTextKeyword = keyword;
        this.fullTextFields = fields;
        return this;
    }

    public Specification<T> build() {
        Specification<T> result = Specification.where(null);

        for (SearchCriteria criteria : criteriaList) {
            if (criteria.getValue() == null && criteria.getOperator() != SearchOperator.IS_NULL
                    && criteria.getOperator() != SearchOperator.IS_NOT_NULL) {
                continue;
            }
            result = result.and(new GenericSpecification<>(criteria));
        }

        if (fullTextKeyword != null && !fullTextKeyword.isBlank() && fullTextFields.length > 0) {
            Specification<T> textSpec = Specification.where(null);
            for (String field : fullTextFields) {
                SearchCriteria fieldCriteria = SearchCriteria.builder()
                        .key(field)
                        .operator(SearchOperator.LIKE)
                        .value(fullTextKeyword)
                        .build();
                textSpec = textSpec.or(new GenericSpecification<>(fieldCriteria));
            }
            result = result.and(textSpec);
        }

        return result;
    }
}
