package com.catalyst.payment.domain.event;

import com.catalyst.payment.domain.model.CancellationReason;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when a subscription is canceled.
 */
@Getter
@Builder
public class SubscriptionCanceled implements DomainEvent {
    
    private final UUID eventId;
    private final Instant timestamp;
    private final UUID subscriptionId;
    private final UUID userId;
    private final CancellationReason reason;
    private final boolean immediate;
    private final LocalDateTime effectiveDate;

    @Override
    public String getEventType() {
        return "SubscriptionCanceled";
    }

    @Override
    public UUID getAggregateId() {
        return subscriptionId;
    }

    public static SubscriptionCanceled of(UUID subscriptionId, UUID userId, CancellationReason reason,
                                         boolean immediate, LocalDateTime effectiveDate) {
        return SubscriptionCanceled.builder()
            .eventId(UUID.randomUUID())
            .timestamp(Instant.now())
            .subscriptionId(subscriptionId)
            .userId(userId)
            .reason(reason)
            .immediate(immediate)
            .effectiveDate(effectiveDate)
            .build();
    }
}

