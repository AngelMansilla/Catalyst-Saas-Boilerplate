package com.catalyst.payment.domain.model;

/**
 * Billing cycle enum for subscriptions.
 */
public enum BillingCycle {
    
    MONTHLY("Monthly", 1),
    ANNUAL("Annual", 12);

    private final String displayName;
    private final int months;

    BillingCycle(String displayName, int months) {
        this.displayName = displayName;
        this.months = months;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMonths() {
        return months;
    }
}

