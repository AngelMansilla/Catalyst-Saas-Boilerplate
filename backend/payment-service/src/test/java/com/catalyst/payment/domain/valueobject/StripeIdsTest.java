package com.catalyst.payment.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Stripe ID value objects.
 */
@DisplayName("Stripe ID Value Objects")
class StripeIdsTest {

    @Nested
    @DisplayName("StripeCustomerId")
    class StripeCustomerIdTest {

        @Test
        @DisplayName("creates valid customer ID")
        void createsValidCustomerId() {
            StripeCustomerId id = StripeCustomerId.of("cus_123456789");

            assertThat(id.getValue()).isEqualTo("cus_123456789");
            assertThat(id.toString()).isEqualTo("cus_123456789");
        }

        @Test
        @DisplayName("throws for null value")
        void throwsForNullValue() {
            assertThatThrownBy(() -> StripeCustomerId.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
        }

        @Test
        @DisplayName("throws for blank value")
        void throwsForBlankValue() {
            assertThatThrownBy(() -> StripeCustomerId.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws for invalid prefix")
        void throwsForInvalidPrefix() {
            assertThatThrownBy(() -> StripeCustomerId.of("sub_123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cus_");
        }

        @Test
        @DisplayName("equals works correctly")
        void equalsWorksCorrectly() {
            StripeCustomerId a = StripeCustomerId.of("cus_abc123");
            StripeCustomerId b = StripeCustomerId.of("cus_abc123");
            StripeCustomerId c = StripeCustomerId.of("cus_xyz789");

            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }

    @Nested
    @DisplayName("StripeSubscriptionId")
    class StripeSubscriptionIdTest {

        @Test
        @DisplayName("creates valid subscription ID")
        void createsValidSubscriptionId() {
            StripeSubscriptionId id = StripeSubscriptionId.of("sub_123456789");

            assertThat(id.getValue()).isEqualTo("sub_123456789");
        }

        @Test
        @DisplayName("throws for null value")
        void throwsForNullValue() {
            assertThatThrownBy(() -> StripeSubscriptionId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws for invalid prefix")
        void throwsForInvalidPrefix() {
            assertThatThrownBy(() -> StripeSubscriptionId.of("cus_123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sub_");
        }
    }

    @Nested
    @DisplayName("StripeInvoiceId")
    class StripeInvoiceIdTest {

        @Test
        @DisplayName("creates valid invoice ID")
        void createsValidInvoiceId() {
            StripeInvoiceId id = StripeInvoiceId.of("in_123456789");

            assertThat(id.getValue()).isEqualTo("in_123456789");
        }

        @Test
        @DisplayName("throws for null value")
        void throwsForNullValue() {
            assertThatThrownBy(() -> StripeInvoiceId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws for invalid prefix")
        void throwsForInvalidPrefix() {
            assertThatThrownBy(() -> StripeInvoiceId.of("sub_123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("in_");
        }
    }

    @Nested
    @DisplayName("StripePaymentIntentId")
    class StripePaymentIntentIdTest {

        @Test
        @DisplayName("creates valid payment intent ID")
        void createsValidPaymentIntentId() {
            StripePaymentIntentId id = StripePaymentIntentId.of("pi_123456789");

            assertThat(id.getValue()).isEqualTo("pi_123456789");
        }

        @Test
        @DisplayName("throws for null value")
        void throwsForNullValue() {
            assertThatThrownBy(() -> StripePaymentIntentId.of(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws for invalid prefix")
        void throwsForInvalidPrefix() {
            assertThatThrownBy(() -> StripePaymentIntentId.of("in_123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pi_");
        }
    }
}

