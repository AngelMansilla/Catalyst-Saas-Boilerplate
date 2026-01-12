package com.catalyst.payment.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when a payment fails.
 */
@Getter
@Builder
public class PaymentFailed implements DomainEvent {
    
    private final UUID eventId;
    private final Instant timestamp;
    private final UUID subscriptionId;
    private final UUID invoiceId;
    private final int attemptNumber;
    private final String failureReason;
    private final LocalDateTime nextRetryDate;

    @Override
    public String getEventType() {
        return "PaymentFailed";
    }

    @Override
    public UUID getAggregateId() {
        return subscriptionId;
    }

    public static PaymentFailed of(UUID subscriptionId, UUID invoiceId, int attemptNumber,
                                  String failureReason, LocalDateTime nextRetryDate) {
        return PaymentFailed.builder()
            .eventId(UUID.randomUUID())
            .timestamp(Instant.now())
            .subscriptionId(subscriptionId)
            .invoiceId(invoiceId)
            .attemptNumber(attemptNumber)
            .failureReason(failureReason)
            .nextRetryDate(nextRetryDate)
            .build();
    }
}

