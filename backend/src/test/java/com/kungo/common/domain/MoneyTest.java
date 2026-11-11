package com.kungo.common.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Money value object")
class MoneyTest {

    @Nested
    @DisplayName("add / subtract")
    class Arithmetic {

        @Test
        @DisplayName("additionne deux montants de même devise")
        void shouldAddSameCurrency() {
            Money a = Money.of(new BigDecimal("100.00"), "XOF");
            Money b = Money.of(new BigDecimal("50.00"), "XOF");

            Money result = a.add(b);

            assertThat(result.getAmount()).isEqualByComparingTo("150.00");
            assertThat(result.getCurrency()).isEqualTo("XOF");
        }

        @Test
        @DisplayName("refuse d'additionner des devises différentes")
        void shouldRejectDifferentCurrencies() {
            Money xof = Money.of(BigDecimal.TEN, "XOF");
            Money eur = Money.of(BigDecimal.TEN, "EUR");

            assertThatThrownBy(() -> xof.add(eur)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("soustrait deux montants de même devise")
        void shouldSubtract() {
            Money a = Money.of(new BigDecimal("100.00"), "XOF");
            Money b = Money.of(new BigDecimal("30.00"), "XOF");

            assertThat(a.subtract(b).getAmount()).isEqualByComparingTo("70.00");
        }
    }

    @Nested
    @DisplayName("percentage")
    class Percentage {

        @Test
        @DisplayName("calcule correctement un pourcentage de commission")
        void shouldCalculatePercentage() {
            Money base = Money.of(new BigDecimal("100000"), "XOF");

            Money commission = base.percentage(new BigDecimal("12"));

            assertThat(commission.getAmount()).isEqualByComparingTo("12000.00");
        }

        @Test
        @DisplayName("arrondit au centime le plus proche")
        void shouldRoundToTwoDecimals() {
            Money base = Money.of(new BigDecimal("33.33"), "XOF");

            Money result = base.percentage(new BigDecimal("10"));

            assertThat(result.getAmount()).isEqualByComparingTo("3.33");
        }
    }
}
