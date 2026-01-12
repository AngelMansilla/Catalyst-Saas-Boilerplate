package com.catalyst.payment.domain.model;

/**
 * Invoice status enum matching Stripe invoice statuses.
 */
public enum InvoiceStatus {
    
    DRAFT("Draft invoice, not yet finalized"),
    OPEN("Open invoice awaiting payment"),
    PAID("Invoice has been paid"),
    VOID("Invoice has been voided"),
    UNCOLLECTIBLE("Invoice marked as uncollectible");

    private final String description;

    InvoiceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPaid() {
        return this == PAID;
    }

    public boolean isOpen() {
        return this == OPEN;
    }
}

