package com.catalyst.payment.domain.exception;

import com.catalyst.shared.domain.exception.EntityNotFoundException;

import java.util.UUID;

/**
 * Exception thrown when a subscription is not found.
 */
public class SubscriptionNotFoundException extends EntityNotFoundException {
    
    public SubscriptionNotFoundException(UUID subscriptionId) {
        super("Subscription", subscriptionId);
    }

    public SubscriptionNotFoundException(String message) {
        super(message);
    }
}

