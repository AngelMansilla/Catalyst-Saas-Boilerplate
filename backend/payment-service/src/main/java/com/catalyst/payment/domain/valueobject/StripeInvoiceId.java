package com.catalyst.payment.domain.valueobject;

import com.catalyst.shared.domain.common.ValueObject;

import java.util.Objects;

/**
 * Value object representing a Stripe Invoice ID.
 * Immutable and validates the format.
 */
public final class StripeInvoiceId implements ValueObject {
    
    private final String value;

    private StripeInvoiceId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Stripe Invoice ID cannot be null or empty");
        }
        if (!value.startsWith("in_")) {
            throw new IllegalArgumentException("Invalid Stripe Invoice ID format: must start with 'in_'");
        }
        this.value = value;
    }

    public static StripeInvoiceId of(String value) {
        return new StripeInvoiceId(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StripeInvoiceId that = (StripeInvoiceId) o;
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

