package com.catalyst.payment.domain.model;

/**
 * Subscription tier enum representing the different pricing plans.
 */
public enum SubscriptionTier {
    
    FREE_TRIAL("Free Trial", 0.00, 0.00),
    PROFESSIONAL("Professional", 29.00, 290.00),
    CLINIC("Clinic", 99.00, 990.00);

    private final String displayName;
    private final double monthlyPrice;
    private final double annualPrice;

    SubscriptionTier(String displayName, double monthlyPrice, double annualPrice) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.annualPrice = annualPrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getMonthlyPrice() {
        return monthlyPrice;
    }

    public double getAnnualPrice() {
        return annualPrice;
    }

    public boolean isFree() {
        return this == FREE_TRIAL;
    }

    public boolean isPaid() {
        return this == PROFESSIONAL || this == CLINIC;
    }
}

