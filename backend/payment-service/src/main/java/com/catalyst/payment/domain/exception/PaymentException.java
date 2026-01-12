package com.catalyst.payment.domain.exception;

import com.catalyst.shared.domain.exception.DomainException;

/**
 * Base exception for payment-related domain errors.
 */
public class PaymentException extends DomainException {
    
    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}

