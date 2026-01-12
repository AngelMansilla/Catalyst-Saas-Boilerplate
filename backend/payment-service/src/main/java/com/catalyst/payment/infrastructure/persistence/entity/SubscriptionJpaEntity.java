package com.catalyst.payment.infrastructure.persistence.entity;

import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.CancellationReason;
import com.catalyst.payment.domain.model.SubscriptionStatus;
import com.catalyst.payment.domain.model.SubscriptionTier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for subscription persistence.
 */
@Entity
@Table(name = "subscriptions", schema = "payment")
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "customer_id", nullable = false, columnDefinition = "uuid")
    private UUID customerId;

    @Column(name = "stripe_subscription_id", unique = true)
    private String stripeSubscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionTier tier;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", length = 20)
    private BillingCycle billingCycle;

    @Column(name = "trial_end_date")
    private LocalDateTime trialEndDate;

    @Column(name = "current_period_start")
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end")
    private LocalDateTime currentPeriodEnd;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", length = 30)
    private CancellationReason cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

