package com.catalyst.payment.domain.model;

import com.catalyst.payment.domain.valueobject.Money;
import com.catalyst.payment.domain.valueobject.StripePaymentIntentId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment entity representing a payment transaction.
 * Framework-agnostic domain entity.
 */
@Getter
public class Payment {
    
    private UUID id;
    private UUID invoiceId;
    private StripePaymentIntentId stripePaymentIntentId;
    private PaymentStatus status;
    private Money amount;
    private String paymentMethodType;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor
    private Payment() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new payment.
     *
     * @param invoiceId the invoice ID
     * @param stripePaymentIntentId the Stripe payment intent ID
     * @param amount the payment amount
     * @param paymentMethodType the payment method type
     * @return a new payment
     */
    public static Payment create(UUID invoiceId, StripePaymentIntentId stripePaymentIntentId,
                                 Money amount, String paymentMethodType) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice ID cannot be null");
        }
        if (stripePaymentIntentId == null) {
            throw new IllegalArgumentException("Stripe Payment Intent ID cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        Payment payment = new Payment();
        payment.invoiceId = invoiceId;
        payment.stripePaymentIntentId = stripePaymentIntentId;
        payment.amount = amount;
        payment.paymentMethodType = paymentMethodType;
        payment.status = PaymentStatus.PENDING;
        
        return payment;
    }

    /**
     * Factory method for reconstituting from persistence.
     *
     * @param invoiceId the invoice ID
     * @param amount the payment amount
     * @return a new payment instance for hydration
     */
    public static Payment create(UUID invoiceId, Money amount) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice ID cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        Payment payment = new Payment();
        payment.invoiceId = invoiceId;
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;
        
        return payment;
    }

    /**
     * Marks the payment as processing.
     */
    public void markAsProcessing() {
        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the payment as succeeded.
     */
    public void markAsSucceeded() {
        this.status = PaymentStatus.SUCCEEDED;
        this.failureReason = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the payment as failed.
     *
     * @param failureReason the failure reason
     */
    public void markAsFailed(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the payment as canceled.
     */
    public void markAsCanceled() {
        this.status = PaymentStatus.CANCELED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the payment was successful.
     *
     * @return true if successful
     */
    public boolean isSuccessful() {
        return this.status == PaymentStatus.SUCCEEDED;
    }

    /**
     * Checks if the payment failed.
     *
     * @return true if failed
     */
    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }

    /**
     * Checks if the payment is still pending.
     *
     * @return true if pending
     */
    public boolean isPending() {
        return this.status == PaymentStatus.PENDING || this.status == PaymentStatus.PROCESSING;
    }

    // Setters for persistence layer (reconstitution)
    public void setId(UUID id) {
        this.id = id;
    }

    public void setInvoiceId(UUID invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void setStripePaymentIntentId(StripePaymentIntentId stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public void setPaymentMethodType(String paymentMethodType) {
        this.paymentMethodType = paymentMethodType;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

