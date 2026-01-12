package com.catalyst.payment.domain.model;

import java.util.Set;

/**
 * Subscription status enum with state transition rules.
 * Implements the state machine for subscription lifecycle.
 */
public enum SubscriptionStatus {
    
    TRIAL(Set.of("ACTIVE", "CANCELED", "EXPIRED")),
    ACTIVE(Set.of("PAST_DUE", "CANCELED")),
    PAST_DUE(Set.of("ACTIVE", "CANCELED")),
    CANCELED(Set.of("TRIAL", "ACTIVE")),  // Reactivation allowed
    EXPIRED(Set.of("TRIAL", "ACTIVE"));    // Reactivation allowed

    private final Set<String> allowedTransitions;

    SubscriptionStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    /**
     * Checks if a transition to the target status is allowed.
     *
     * @param target the target status
     * @return true if the transition is allowed, false otherwise
     */
    public boolean canTransitionTo(SubscriptionStatus target) {
        return allowedTransitions.contains(target.name());
    }

    /**
     * Gets the set of allowed transition status names.
     *
     * @return set of allowed status names
     */
    public Set<String> getAllowedTransitions() {
        return Set.copyOf(allowedTransitions);
    }
}

