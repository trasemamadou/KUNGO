package com.kungo.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.kungo.category.Category;
import com.kungo.common.domain.Money;
import com.kungo.payment.domain.entity.CommissionRule;
import com.kungo.payment.repository.CommissionRuleRepository;
import com.kungo.payment.service.impl.PercentageCommissionCalculator;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PercentageCommissionCalculator")
class PercentageCommissionCalculatorTest {

    @Mock private CommissionRuleRepository commissionRuleRepository;

    @InjectMocks
    private PercentageCommissionCalculator calculator;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(calculator, "defaultPercentage", new BigDecimal("12.0"));
    }

    @Test
    @DisplayName("utilise la règle spécifique à la catégorie si elle existe")
    void shouldUseCategorySpecificRule() {
        Category category = Category.builder().name("Musique").build();
        category.setId(UUID.randomUUID());
        CommissionRule categoryRule = CommissionRule.builder().percentage(new BigDecimal("8.0")).build();
        when(commissionRuleRepository.findFirstByCategoryIdAndActiveTrueOrderByEffectiveFromDesc(category.getId()))
                .thenReturn(Optional.of(categoryRule));

        BigDecimal percentage = calculator.resolvePercentage(category);

        assertThat(percentage).isEqualByComparingTo("8.0");
    }

    @Test
    @DisplayName("retombe sur la règle globale si aucune règle catégorie n'existe")
    void shouldFallBackToGlobalRule() {
        Category category = Category.builder().name("Photographie").build();
        category.setId(UUID.randomUUID());
        when(commissionRuleRepository.findFirstByCategoryIdAndActiveTrueOrderByEffectiveFromDesc(category.getId()))
                .thenReturn(Optional.empty());
        CommissionRule globalRule = CommissionRule.builder().percentage(new BigDecimal("10.0")).build();
        when(commissionRuleRepository.findFirstByCategoryIsNullAndActiveTrueOrderByEffectiveFromDesc())
                .thenReturn(Optional.of(globalRule));

        BigDecimal percentage = calculator.resolvePercentage(category);

        assertThat(percentage).isEqualByComparingTo("10.0");
    }

    @Test
    @DisplayName("retombe sur la valeur de configuration par défaut si aucune règle n'existe en base")
    void shouldFallBackToDefaultConfigValue() {
        when(commissionRuleRepository.findFirstByCategoryIsNullAndActiveTrueOrderByEffectiveFromDesc())
                .thenReturn(Optional.empty());

        BigDecimal percentage = calculator.resolvePercentage(null);

        assertThat(percentage).isEqualByComparingTo("12.0");
    }

    @Test
    @DisplayName("calculate() applique le pourcentage résolu au montant de base")
    void shouldCalculateCommissionAmount() {
        when(commissionRuleRepository.findFirstByCategoryIsNullAndActiveTrueOrderByEffectiveFromDesc())
                .thenReturn(Optional.of(CommissionRule.builder().percentage(new BigDecimal("12.0")).build()));

        Money commission = calculator.calculate(Money.of(new BigDecimal("100000"), "XOF"), null);

        assertThat(commission.getAmount()).isEqualByComparingTo("12000.00");
    }
}
