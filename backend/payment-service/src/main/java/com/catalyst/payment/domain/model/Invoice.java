package com.catalyst.payment.domain.model;

import com.catalyst.payment.domain.valueobject.Money;
import com.catalyst.payment.domain.valueobject.StripeInvoiceId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Invoice entity representing a billing invoice.
 * Framework-agnostic domain entity.
 */
@Getter
public class Invoice {
    
    private UUID id;
    private UUID subscriptionId;
    private StripeInvoiceId stripeInvoiceId;
    private InvoiceStatus status;
    private Money amountDue;
    private Money amountPaid;
    private String invoicePdfUrl;
    private String hostedInvoiceUrl;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor
    private Invoice() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new invoice.
     *
     * @param subscriptionId the subscription ID
     * @param stripeInvoiceId the Stripe invoice ID
     * @param amountDue the amount due
     * @param dueDate the due date
     * @return a new invoice
     */
    public static Invoice create(UUID subscriptionId, StripeInvoiceId stripeInvoiceId, 
                                 Money amountDue, LocalDateTime dueDate) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException("Subscription ID cannot be null");
        }
        if (stripeInvoiceId == null) {
            throw new IllegalArgumentException("Stripe Invoice ID cannot be null");
        }
        if (amountDue == null) {
            throw new IllegalArgumentException("Amount due cannot be null");
        }

        Invoice invoice = new Invoice();
        invoice.subscriptionId = subscriptionId;
        invoice.stripeInvoiceId = stripeInvoiceId;
        invoice.status = InvoiceStatus.OPEN;
        invoice.amountDue = amountDue;
        invoice.amountPaid = Money.zero(amountDue.getCurrency());
        invoice.dueDate = dueDate;
        
        return invoice;
    }

    /**
     * Marks the invoice as paid.
     *
     * @param amountPaid the amount paid
     * @param paidAt the payment timestamp
     */
    public void markAsPaid(Money amountPaid, LocalDateTime paidAt) {
        if (amountPaid == null) {
            throw new IllegalArgumentException("Amount paid cannot be null");
        }
        if (paidAt == null) {
            throw new IllegalArgumentException("Paid at timestamp cannot be null");
        }

        this.status = InvoiceStatus.PAID;
        this.amountPaid = amountPaid;
        this.paidAt = paidAt;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the invoice as void.
     */
    public void markAsVoid() {
        this.status = InvoiceStatus.VOID;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the invoice as uncollectible.
     */
    public void markAsUncollectible() {
        this.status = InvoiceStatus.UNCOLLECTIBLE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the invoice URLs from Stripe.
     *
     * @param pdfUrl the PDF URL
     * @param hostedUrl the hosted invoice URL
     */
    public void updateUrls(String pdfUrl, String hostedUrl) {
        this.invoicePdfUrl = pdfUrl;
        this.hostedInvoiceUrl = hostedUrl;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the invoice is paid.
     *
     * @return true if paid
     */
    public boolean isPaid() {
        return this.status == InvoiceStatus.PAID;
    }

    /**
     * Checks if the invoice is overdue.
     *
     * @return true if overdue
     */
    public boolean isOverdue() {
        return this.status == InvoiceStatus.OPEN 
            && this.dueDate != null 
            && LocalDateTime.now().isAfter(this.dueDate);
    }

    // Package-private setters for persistence layer
    void setId(UUID id) {
        this.id = id;
    }

    void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    void setStripeInvoiceId(StripeInvoiceId stripeInvoiceId) {
        this.stripeInvoiceId = stripeInvoiceId;
    }

    void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    void setAmountDue(Money amountDue) {
        this.amountDue = amountDue;
    }

    void setAmountPaid(Money amountPaid) {
        this.amountPaid = amountPaid;
    }

    void setInvoicePdfUrl(String invoicePdfUrl) {
        this.invoicePdfUrl = invoicePdfUrl;
    }

    void setHostedInvoiceUrl(String hostedInvoiceUrl) {
        this.hostedInvoiceUrl = hostedInvoiceUrl;
    }

    void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

