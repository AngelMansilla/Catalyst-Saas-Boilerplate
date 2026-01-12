package com.catalyst.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for webhook event idempotency tracking.
 */
@Entity
@Table(name = "webhook_events", schema = "payment", 
    indexes = @Index(name = "idx_stripe_event_id", columnList = "stripe_event_id", unique = true))
@Getter
@Setter
@NoArgsConstructor
public class WebhookEventJpaEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "stripe_event_id", nullable = false, unique = true)
    private String stripeEventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    public WebhookEventJpaEntity(String stripeEventId, String eventType, String payload) {
        this.id = UUID.randomUUID();
        this.stripeEventId = stripeEventId;
        this.eventType = eventType;
        this.payload = payload;
    }
}

