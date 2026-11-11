package com.kungo.payment.service;

import com.kungo.category.Category;
import com.kungo.common.domain.Money;
import java.math.BigDecimal;

/**
 * Stratégie de calcul de la commission plateforme sur un montant donné.
 */
public interface CommissionCalculator {

    Money calculate(Money base, Category category);

    BigDecimal resolvePercentage(Category category);
}
