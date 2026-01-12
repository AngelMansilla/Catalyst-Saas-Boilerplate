package com.catalyst.payment.domain.valueobject;

import com.catalyst.shared.domain.common.ValueObject;

import java.util.Objects;

/**
 * Value object representing a Stripe Subscription ID.
 * Immutable and validates the format.
 */
public final class StripeSubscriptionId implements ValueObject {
    
    private final String value;

    private StripeSubscriptionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Stripe Subscription ID cannot be null or empty");
        }
        if (!value.startsWith("sub_")) {
            throw new IllegalArgumentException("Invalid Stripe Subscription ID format: must start with 'sub_'");
        }
        this.value = value;
    }

    public static StripeSubscriptionId of(String value) {
        return new StripeSubscriptionId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StripeSubscriptionId that = (StripeSubscriptionId) o;
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

