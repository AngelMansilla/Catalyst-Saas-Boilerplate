package com.catalyst.payment.domain.exception;

import com.catalyst.shared.domain.exception.BusinessRuleViolationException;

/**
 * Exception thrown when an invalid subscription state transition is attempted.
 */
public class InvalidSubscriptionStateException extends BusinessRuleViolationException {
    
    public InvalidSubscriptionStateException(String message) {
        super(message);
    }

    public InvalidSubscriptionStateException(String from, String to) {
        super(String.format("Cannot transition subscription from %s to %s", from, to));
    }
}

