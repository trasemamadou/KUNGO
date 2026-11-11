package com.kungo.common.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SearchCriteriaParserTest {

    @Test
    void shouldReturnEmptyListForBlankInput() {
        assertThat(SearchCriteriaParser.parse(null)).isEmpty();
        assertThat(SearchCriteriaParser.parse("  ")).isEmpty();
    }

    @Test
    void shouldParseSimpleEqualsCriteria() {
        List<SearchCriteria> criteria = SearchCriteriaParser.parse("status:EQUALS:ACTIVE");

        assertThat(criteria).hasSize(1);
        assertThat(criteria.get(0).getKey()).isEqualTo("status");
        assertThat(criteria.get(0).getOperator()).isEqualTo(SearchOperator.EQUALS);
        assertThat(criteria.get(0).getValue()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldParseMultipleCommaSeparatedCriteria() {
        List<SearchCriteria> criteria = SearchCriteriaParser.parse("status:EQUALS:ACTIVE,role:EQUALS:CLIENT");

        assertThat(criteria).hasSize(2);
    }

    @Test
    void shouldParseBetweenOperatorWithPipeSeparatedRange() {
        List<SearchCriteria> criteria = SearchCriteriaParser.parse("price:BETWEEN:100|500");

        assertThat(criteria).hasSize(1);
        assertThat(criteria.get(0).getOperator()).isEqualTo(SearchOperator.BETWEEN);
        assertThat(criteria.get(0).getValue()).isEqualTo("100");
        assertThat(criteria.get(0).getValueTo()).isEqualTo("500");
    }

    @Test
    void shouldParseInOperatorAsList() {
        List<SearchCriteria> criteria = SearchCriteriaParser.parse("role:IN:CLIENT|PROFESSIONNEL");

        assertThat(criteria.get(0).getValue()).isEqualTo(List.of("CLIENT", "PROFESSIONNEL"));
    }

    @Test
    void shouldParseUnaryOperatorsWithoutValue() {
        List<SearchCriteria> criteria = SearchCriteriaParser.parse("deletedAt:IS_NULL");

        assertThat(criteria).hasSize(1);
        assertThat(criteria.get(0).getOperator()).isEqualTo(SearchOperator.IS_NULL);
    }
}
