package com.catalyst.user.domain.event;

import com.catalyst.user.domain.valueobject.Email;
import com.catalyst.user.domain.valueobject.UserId;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a user is deleted (GDPR Right to Erasure).
 * 
 * <p>
 * This event is consumed by all services to trigger
 * permanent deletion of associated user data.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record UserDeleted(
        UUID eventId,
        Instant timestamp,
        UserId userId,
        Email email) {
    public UserDeleted(UserId userId, Email email) {
        this(UUID.randomUUID(), Instant.now(), userId, email);
    }

    public String getEventType() {
        return "UserDeleted";
    }

    public String getVersion() {
        return "1.0";
    }
}
