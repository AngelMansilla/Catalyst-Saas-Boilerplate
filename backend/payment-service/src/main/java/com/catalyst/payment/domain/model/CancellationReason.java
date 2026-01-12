package com.catalyst.payment.domain.model;

/**
 * Reasons for subscription cancellation.
 */
public enum CancellationReason {
    
    USER_REQUESTED("User requested cancellation"),
    PAYMENT_FAILED("Payment failed after retries"),
    TRIAL_EXPIRED("Trial period expired without conversion"),
    ADMIN_ACTION("Administrative action"),
    FRAUD_DETECTED("Fraud detected"),
    OTHER("Other reason");

    private final String description;

    CancellationReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

