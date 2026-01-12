package com.catalyst.payment.domain.model;

/**
 * Payment status enum for tracking payment transactions.
 */
public enum PaymentStatus {
    
    PENDING("Payment is pending"),
    PROCESSING("Payment is being processed"),
    SUCCEEDED("Payment succeeded"),
    FAILED("Payment failed"),
    CANCELED("Payment was canceled");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSuccessful() {
        return this == SUCCEEDED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isPending() {
        return this == PENDING || this == PROCESSING;
    }
}

