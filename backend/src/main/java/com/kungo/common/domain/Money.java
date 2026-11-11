package com.kungo.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Value object monétaire immuable en usage (les opérations retournent une nouvelle
 * instance). Toujours manipuler un montant avec sa devise pour éviter les erreurs
 * de conversion implicite entre professionnels de pays différents.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@EqualsAndHashCode
@ToString
public class Money {

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "XOF";

    public static Money of(BigDecimal amount, String currency) {
        return Money.builder().amount(amount).currency(currency).build();
    }

    public static Money zero(String currency) {
        return Money.of(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return Money.of(this.amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return Money.of(this.amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal factor) {
        return Money.of(this.amount.multiply(factor).setScale(2, RoundingMode.HALF_UP), currency);
    }

    public Money percentage(BigDecimal percentage) {
        return multiply(percentage.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot operate on different currencies: "
                    + this.currency + " vs " + other.currency);
        }
    }
}
