package com.catalyst.payment.domain.valueobject;

import com.catalyst.shared.domain.common.ValueObject;

import java.util.Objects;

/**
 * Value object representing a Stripe Payment Intent ID.
 * Immutable and validates the format.
 */
public final class StripePaymentIntentId implements ValueObject {
    
    private final String value;

    private StripePaymentIntentId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Stripe Payment Intent ID cannot be null or empty");
        }
        if (!value.startsWith("pi_")) {
            throw new IllegalArgumentException("Invalid Stripe Payment Intent ID format: must start with 'pi_'");
        }
        this.value = value;
    }

    public static StripePaymentIntentId of(String value) {
        return new StripePaymentIntentId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StripePaymentIntentId that = (StripePaymentIntentId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

