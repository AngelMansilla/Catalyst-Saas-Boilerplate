package com.catalyst.payment.domain.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a subscription trial expires.
 */
@Getter
public class TrialExpired implements DomainEvent {
    
    private final UUID eventId;
    private final UUID aggregateId;
    private final UUID userId;
    private final Instant timestamp;
    private final String eventType;

    private TrialExpired(UUID subscriptionId, UUID userId) {
        this.eventId = UUID.randomUUID();
        this.aggregateId = subscriptionId;
        this.userId = userId;
        this.timestamp = Instant.now();
        this.eventType = "TrialExpired";
    }

    public static TrialExpired of(UUID subscriptionId, UUID userId) {
        return new TrialExpired(subscriptionId, userId);
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}

