package com.catalyst.payment.domain.model;

import com.catalyst.payment.domain.exception.InvalidSubscriptionStateException;
import com.catalyst.payment.domain.valueobject.StripeSubscriptionId;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subscription aggregate root implementing the state machine.
 * This is a framework-agnostic domain entity (no JPA annotations).
 */
@Getter
public class Subscription {
    
    private UUID id;
    private UUID customerId;
    private StripeSubscriptionId stripeSubscriptionId;
    private SubscriptionStatus status;
    private SubscriptionTier tier;
    private BillingCycle billingCycle;
    private LocalDateTime trialEndDate;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime canceledAt;
    private CancellationReason cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor for factory methods
    private Subscription() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new trial subscription.
     *
     * @param customerId the customer ID
     * @param tier the subscription tier
     * @param trialDays the number of trial days
     * @return a new trial subscription
     */
    public static Subscription startTrial(UUID customerId, SubscriptionTier tier, int trialDays) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID cannot be null");
        }
        if (tier == null) {
            throw new IllegalArgumentException("Subscription tier cannot be null");
        }
        if (trialDays <= 0) {
            throw new IllegalArgumentException("Trial days must be positive");
        }

        Subscription subscription = new Subscription();
        subscription.customerId = customerId;
        subscription.tier = tier;
        subscription.status = SubscriptionStatus.TRIAL;
        subscription.trialEndDate = LocalDateTime.now().plusDays(trialDays);
        
        return subscription;
    }

    /**
     * Activates the subscription (converts from trial or reactivates).
     *
     * @param stripeSubscriptionId the Stripe subscription ID
     * @param billingCycle the billing cycle
     */
    public void activate(StripeSubscriptionId stripeSubscriptionId, BillingCycle billingCycle) {
        validateTransition(SubscriptionStatus.ACTIVE);
        
        this.status = SubscriptionStatus.ACTIVE;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.billingCycle = billingCycle;
        this.trialEndDate = null;
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = calculatePeriodEnd(billingCycle);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the subscription as past due (payment failed).
     */
    public void markPastDue() {
        validateTransition(SubscriptionStatus.PAST_DUE);
        
        this.status = SubscriptionStatus.PAST_DUE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Records a successful payment and returns subscription to active if past due.
     */
    public void recordPaymentSuccess() {
        if (this.status == SubscriptionStatus.PAST_DUE) {
            this.status = SubscriptionStatus.ACTIVE;
            this.currentPeriodStart = LocalDateTime.now();
            this.currentPeriodEnd = calculatePeriodEnd(this.billingCycle);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancels the subscription.
     *
     * @param reason the cancellation reason
     * @param immediate whether to cancel immediately or at period end
     */
    public void cancel(CancellationReason reason, boolean immediate) {
        validateTransition(SubscriptionStatus.CANCELED);
        
        this.status = SubscriptionStatus.CANCELED;
        this.cancellationReason = reason;
        this.canceledAt = LocalDateTime.now();
        
        if (!immediate && this.currentPeriodEnd != null) {
            // Cancel at period end - subscription remains active until then
            // This would be handled by a scheduled job
        }
        
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the trial as expired.
     */
    public void expireTrial() {
        if (this.status != SubscriptionStatus.TRIAL) {
            throw new InvalidSubscriptionStateException("Can only expire subscriptions in TRIAL status");
        }
        
        this.status = SubscriptionStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the subscription is currently in trial period.
     *
     * @return true if in trial and trial has not expired
     */
    public boolean isInTrial() {
        return this.status == SubscriptionStatus.TRIAL 
            && this.trialEndDate != null 
            && LocalDateTime.now().isBefore(this.trialEndDate);
    }

    /**
     * Checks if the subscription is active (not past due, canceled, or expired).
     *
     * @return true if active
     */
    public boolean isActive() {
        return this.status == SubscriptionStatus.ACTIVE;
    }

    /**
     * Checks if the subscription is past due.
     *
     * @return true if past due
     */
    public boolean isPastDue() {
        return this.status == SubscriptionStatus.PAST_DUE;
    }

    /**
     * Checks if the subscription is canceled.
     *
     * @return true if canceled
     */
    public boolean isCanceled() {
        return this.status == SubscriptionStatus.CANCELED;
    }

    /**
     * Checks if the subscription can be reactivated.
     *
     * @return true if can be reactivated
     */
    public boolean canReactivate() {
        return this.status == SubscriptionStatus.CANCELED || this.status == SubscriptionStatus.EXPIRED;
    }

    /**
     * Updates the current period dates.
     *
     * @param start the period start date
     * @param end the period end date
     */
    public void updatePeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Period dates cannot be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Period start must be before period end");
        }
        
        this.currentPeriodStart = start;
        this.currentPeriodEnd = end;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates if a state transition is allowed.
     *
     * @param targetStatus the target status
     * @throws InvalidSubscriptionStateException if transition is not allowed
     */
    private void validateTransition(SubscriptionStatus targetStatus) {
        if (!this.status.canTransitionTo(targetStatus)) {
            throw new InvalidSubscriptionStateException(this.status.name(), targetStatus.name());
        }
    }

    /**
     * Calculates the period end date based on billing cycle.
     *
     * @param cycle the billing cycle
     * @return the period end date
     */
    private LocalDateTime calculatePeriodEnd(BillingCycle cycle) {
        if (cycle == null) {
            return null;
        }
        return LocalDateTime.now().plusMonths(cycle.getMonths());
    }

    // Package-private setters for JPA/persistence layer
    void setId(UUID id) {
        this.id = id;
    }

    void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    void setStripeSubscriptionId(StripeSubscriptionId stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    void setTier(SubscriptionTier tier) {
        this.tier = tier;
    }

    void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    void setTrialEndDate(LocalDateTime trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    void setCurrentPeriodStart(LocalDateTime currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }

    void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

