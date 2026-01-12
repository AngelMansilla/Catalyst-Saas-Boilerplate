package com.catalyst.payment.domain.event;

import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.SubscriptionTier;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when a subscription is activated.
 */
@Getter
@Builder
public class SubscriptionActivated implements DomainEvent {
    
    private final UUID eventId;
    private final Instant timestamp;
    private final UUID subscriptionId;
    private final UUID userId;
    private final SubscriptionTier tier;
    private final BillingCycle billingCycle;
    private final String stripeSubscriptionId;
    private final LocalDateTime currentPeriodEnd;

    @Override
    public String getEventType() {
        return "SubscriptionActivated";
    }

    @Override
    public UUID getAggregateId() {
        return subscriptionId;
    }

    public static SubscriptionActivated of(UUID subscriptionId, UUID userId, SubscriptionTier tier,
                                          BillingCycle billingCycle, String stripeSubscriptionId,
                                          LocalDateTime currentPeriodEnd) {
        return SubscriptionActivated.builder()
            .eventId(UUID.randomUUID())
            .timestamp(Instant.now())
            .subscriptionId(subscriptionId)
            .userId(userId)
            .tier(tier)
            .billingCycle(billingCycle)
            .stripeSubscriptionId(stripeSubscriptionId)
            .currentPeriodEnd(currentPeriodEnd)
            .build();
    }
}

