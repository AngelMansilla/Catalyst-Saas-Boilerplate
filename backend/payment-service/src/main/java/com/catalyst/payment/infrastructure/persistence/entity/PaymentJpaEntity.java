package com.catalyst.payment.infrastructure.persistence.entity;

import com.catalyst.payment.domain.model.PaymentStatus;
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
 * JPA entity for payment persistence.
 */
@Entity
@Table(name = "payments", schema = "payment")
@Getter
@Setter
@NoArgsConstructor
public class PaymentJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "invoice_id", nullable = false, columnDefinition = "uuid")
    private UUID invoiceId;

    @Column(name = "stripe_payment_intent_id", unique = true)
    private String stripePaymentIntentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "payment_method_type", length = 50)
    private String paymentMethodType;

    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

