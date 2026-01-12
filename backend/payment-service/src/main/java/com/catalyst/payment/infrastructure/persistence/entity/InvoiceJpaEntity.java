package com.catalyst.payment.infrastructure.persistence.entity;

import com.catalyst.payment.domain.model.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for invoice persistence.
 */
@Entity
@Table(name = "invoices", schema = "payment")
@Getter
@Setter
@NoArgsConstructor
public class InvoiceJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "subscription_id", nullable = false, columnDefinition = "uuid")
    private UUID subscriptionId;

    @Column(name = "stripe_invoice_id", unique = true)
    private String stripeInvoiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "amount_due", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountDue;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "invoice_pdf_url", length = 500)
    private String invoicePdfUrl;

    @Column(name = "hosted_invoice_url", length = 500)
    private String hostedInvoiceUrl;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

