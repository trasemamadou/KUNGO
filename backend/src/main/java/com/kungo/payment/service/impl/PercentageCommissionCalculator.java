package com.kungo.payment.service.impl;

import com.kungo.category.Category;
import com.kungo.common.domain.Money;
import com.kungo.payment.domain.entity.CommissionRule;
import com.kungo.payment.repository.CommissionRuleRepository;
import com.kungo.payment.service.CommissionCalculator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Résout le taux applicable en cherchant d'abord une règle spécifique à la
 * catégorie de la prestation, puis la règle globale, puis la valeur par défaut
 * de configuration ({@code app.commission.default-percentage}).
 */
@Component
@RequiredArgsConstructor
public class PercentageCommissionCalculator implements CommissionCalculator {

    private final CommissionRuleRepository commissionRuleRepository;

    @Value("${app.commission.default-percentage}")
    private BigDecimal defaultPercentage;

    @Override
    public Money calculate(Money base, Category category) {
        BigDecimal percentage = resolvePercentage(category);
        return base.percentage(percentage);
    }

    @Override
    public BigDecimal resolvePercentage(Category category) {
        if (category != null) {
            var categoryRule = commissionRuleRepository
                    .findFirstByCategoryIdAndActiveTrueOrderByEffectiveFromDesc(category.getId());
            if (categoryRule.isPresent()) {
                return categoryRule.get().getPercentage();
            }
        }
        return commissionRuleRepository.findFirstByCategoryIsNullAndActiveTrueOrderByEffectiveFromDesc()
                .map(CommissionRule::getPercentage)
                .orElse(defaultPercentage);
    }
}
