package com.catalyst.payment.domain.event;

import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.SubscriptionTier;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when a subscription is created.
 */
@Getter
@Builder
public class SubscriptionCreated implements DomainEvent {
    
    private final UUID eventId;
    private final Instant timestamp;
    private final UUID subscriptionId;
    private final UUID userId;
    private final SubscriptionTier tier;
    private final BillingCycle billingCycle;
    private final LocalDateTime trialEndDate;
    private final Double monthlyPrice;
    private final String currency;

    @Override
    public String getEventType() {
        return "SubscriptionCreated";
    }

    @Override
    public UUID getAggregateId() {
        return subscriptionId;
    }

    public static SubscriptionCreated of(UUID subscriptionId, UUID userId, SubscriptionTier tier,
                                        LocalDateTime trialEndDate) {
        return SubscriptionCreated.builder()
            .eventId(UUID.randomUUID())
            .timestamp(Instant.now())
            .subscriptionId(subscriptionId)
            .userId(userId)
            .tier(tier)
            .trialEndDate(trialEndDate)
            .monthlyPrice(tier.getMonthlyPrice())
            .currency("USD")
            .build();
    }
}

