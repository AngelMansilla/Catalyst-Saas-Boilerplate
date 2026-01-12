package com.catalyst.payment.domain.valueobject;

import com.catalyst.shared.domain.common.ValueObject;

import java.util.Objects;

/**
 * Value object representing a Stripe Customer ID.
 * Immutable and validates the format.
 */
public final class StripeCustomerId implements ValueObject {
    
    private final String value;

    private StripeCustomerId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Stripe Customer ID cannot be null or empty");
        }
        if (!value.startsWith("cus_")) {
            throw new IllegalArgumentException("Invalid Stripe Customer ID format: must start with 'cus_'");
        }
        this.value = value;
    }

    public static StripeCustomerId of(String value) {
        return new StripeCustomerId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StripeCustomerId that = (StripeCustomerId) o;
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

