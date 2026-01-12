package com.catalyst.payment.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events.
 */
public interface DomainEvent {
    
    /**
     * Gets the unique event ID.
     *
     * @return the event ID
     */
    UUID getEventId();

    /**
     * Gets the event type name.
     *
     * @return the event type
     */
    String getEventType();

    /**
     * Gets the event timestamp.
     *
     * @return the timestamp
     */
    Instant getTimestamp();

    /**
     * Gets the aggregate ID that this event relates to.
     *
     * @return the aggregate ID
     */
    UUID getAggregateId();
}

