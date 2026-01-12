package com.catalyst.payment.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Money value object.
 */
@DisplayName("Money Value Object")
class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("creates Money from BigDecimal and Currency")
        void createsFromBigDecimalAndCurrency() {
            Money money = Money.of(BigDecimal.valueOf(100.50), USD);

            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.50));
            assertThat(money.getCurrency()).isEqualTo(USD);
            assertThat(money.getCurrencyCode()).isEqualTo("USD");
        }

        @Test
        @DisplayName("creates Money from double and Currency")
        void createsFromDoubleAndCurrency() {
            Money money = Money.of(99.99, USD);

            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
        }

        @Test
        @DisplayName("creates USD Money using usd() factory")
        void createsUsdMoney() {
            Money money = Money.usd(50.00);

            assertThat(money.getCurrencyCode()).isEqualTo("USD");
            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        }

        @Test
        @DisplayName("creates zero Money")
        void createsZeroMoney() {
            Money money = Money.zero(USD);

            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(money.isZero()).isTrue();
        }

        @Test
        @DisplayName("throws exception for null amount")
        void throwsForNullAmount() {
            assertThatThrownBy(() -> Money.of((BigDecimal) null, USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Amount");
        }

        @Test
        @DisplayName("throws exception for null currency")
        void throwsForNullCurrency() {
            assertThatThrownBy(() -> Money.of(BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency");
        }

        @Test
        @DisplayName("throws exception for negative amount")
        void throwsForNegativeAmount() {
            assertThatThrownBy(() -> Money.of(BigDecimal.valueOf(-10), USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
        }

        @Test
        @DisplayName("rounds to 2 decimal places")
        void roundsToTwoDecimalPlaces() {
            Money money = Money.of(BigDecimal.valueOf(100.555), USD);

            assertThat(money.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.56));
        }
    }

    @Nested
    @DisplayName("Arithmetic Operations")
    class ArithmeticOperations {

        @Test
        @DisplayName("adds two Money values")
        void addsMoney() {
            Money a = Money.usd(100.00);
            Money b = Money.usd(50.50);

            Money result = a.add(b);

            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.50));
        }

        @Test
        @DisplayName("subtracts two Money values")
        void subtractsMoney() {
            Money a = Money.usd(100.00);
            Money b = Money.usd(30.00);

            Money result = a.subtract(b);

            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(70.00));
        }

        @Test
        @DisplayName("multiplies by BigDecimal")
        void multipliesByBigDecimal() {
            Money money = Money.usd(100.00);

            Money result = money.multiply(BigDecimal.valueOf(1.5));

            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        }

        @Test
        @DisplayName("multiplies by double")
        void multipliesByDouble() {
            Money money = Money.usd(100.00);

            Money result = money.multiply(2.0);

            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        }

        @Test
        @DisplayName("throws when adding different currencies")
        void throwsWhenAddingDifferentCurrencies() {
            Money usd = Money.usd(100.00);
            Money eur = Money.of(50.00, EUR);

            assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different currencies");
        }

        @Test
        @DisplayName("throws when subtracting different currencies")
        void throwsWhenSubtractingDifferentCurrencies() {
            Money usd = Money.usd(100.00);
            Money eur = Money.of(50.00, EUR);

            assertThatThrownBy(() -> usd.subtract(eur))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different currencies");
        }
    }

    @Nested
    @DisplayName("Comparison")
    class Comparison {

        @Test
        @DisplayName("isZero returns true for zero amount")
        void isZeroReturnsTrue() {
            assertThat(Money.zero(USD).isZero()).isTrue();
            assertThat(Money.usd(0.00).isZero()).isTrue();
        }

        @Test
        @DisplayName("isZero returns false for non-zero amount")
        void isZeroReturnsFalse() {
            assertThat(Money.usd(0.01).isZero()).isFalse();
            assertThat(Money.usd(100.00).isZero()).isFalse();
        }

        @Test
        @DisplayName("isPositive returns true for positive amount")
        void isPositiveReturnsTrue() {
            assertThat(Money.usd(0.01).isPositive()).isTrue();
            assertThat(Money.usd(100.00).isPositive()).isTrue();
        }

        @Test
        @DisplayName("isPositive returns false for zero amount")
        void isPositiveReturnsFalse() {
            assertThat(Money.zero(USD).isPositive()).isFalse();
        }

        @Test
        @DisplayName("compareTo works correctly")
        void compareToWorksCorrectly() {
            Money smaller = Money.usd(50.00);
            Money larger = Money.usd(100.00);
            Money equal = Money.usd(50.00);

            assertThat(smaller.compareTo(larger)).isLessThan(0);
            assertThat(larger.compareTo(smaller)).isGreaterThan(0);
            assertThat(smaller.compareTo(equal)).isEqualTo(0);
        }

        @Test
        @DisplayName("compareTo throws for different currencies")
        void compareToThrowsForDifferentCurrencies() {
            Money usd = Money.usd(100.00);
            Money eur = Money.of(100.00, EUR);

            assertThatThrownBy(() -> usd.compareTo(eur))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("equals returns true for same amount and currency")
        void equalsReturnsTrueForSameValues() {
            Money a = Money.usd(100.00);
            Money b = Money.usd(100.00);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("equals returns false for different amounts")
        void equalsReturnsFalseForDifferentAmounts() {
            Money a = Money.usd(100.00);
            Money b = Money.usd(99.99);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals returns false for different currencies")
        void equalsReturnsFalseForDifferentCurrencies() {
            Money usd = Money.usd(100.00);
            Money eur = Money.of(100.00, EUR);

            assertThat(usd).isNotEqualTo(eur);
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("toString includes currency symbol and amount")
        void toStringIncludesCurrencyAndAmount() {
            Money money = Money.usd(99.99);

            String result = money.toString();

            assertThat(result).contains("$");
            assertThat(result).contains("99.99");
        }
    }
}

