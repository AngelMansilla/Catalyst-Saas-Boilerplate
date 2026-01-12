package com.catalyst.payment.infrastructure.kafka;

import com.catalyst.payment.application.ports.output.EventPublisher;
import com.catalyst.payment.domain.event.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter implementing EventPublisher using Kafka.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.kafka.topics.subscription-created:subscription.created}")
    private String subscriptionCreatedTopic;

    @Value("${payment.kafka.topics.subscription-activated:subscription.activated}")
    private String subscriptionActivatedTopic;

    @Value("${payment.kafka.topics.subscription-canceled:subscription.canceled}")
    private String subscriptionCanceledTopic;

    @Value("${payment.kafka.topics.payment-succeeded:payment.succeeded}")
    private String paymentSucceededTopic;

    @Value("${payment.kafka.topics.payment-failed:payment.failed}")
    private String paymentFailedTopic;

    @Override
    public void publish(DomainEvent event, String correlationId) {
        String topic = getTopicForEvent(event);
        publish(event, topic, correlationId);
    }

    @Override
    public void publish(DomainEvent event, String topic, String correlationId) {
        try {
            EventEnvelope envelope = createEnvelope(event, correlationId);
            String payload = objectMapper.writeValueAsString(envelope);
            String key = event.getAggregateId().toString();

            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, key, payload);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event {} to topic {}: {}", 
                        event.getEventType(), topic, ex.getMessage());
                } else {
                    log.info("Published event {} to topic {} with key {} at offset {}", 
                        event.getEventType(), topic, key, 
                        result.getRecordMetadata().offset());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event: {}", event.getEventType(), e);
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private String getTopicForEvent(DomainEvent event) {
        return switch (event.getEventType()) {
            case "SubscriptionCreated" -> subscriptionCreatedTopic;
            case "SubscriptionActivated" -> subscriptionActivatedTopic;
            case "SubscriptionCanceled" -> subscriptionCanceledTopic;
            case "PaymentSucceeded" -> paymentSucceededTopic;
            case "PaymentFailed" -> paymentFailedTopic;
            case "TrialExpired" -> subscriptionCanceledTopic; // Use same topic
            default -> "payment.events"; // Default topic
        };
    }

    private EventEnvelope createEnvelope(DomainEvent event, String correlationId) {
        return new EventEnvelope(
            event.getEventId().toString(),
            event.getEventType(),
            event.getVersion(),
            Instant.now().toString(),
            correlationId != null ? correlationId : UUID.randomUUID().toString(),
            "payment-service",
            event.getAggregateId().toString(),
            event
        );
    }

    /**
     * Event envelope for Kafka messages.
     */
    public record EventEnvelope(
        String eventId,
        String eventType,
        String version,
        String timestamp,
        String correlationId,
        String source,
        String aggregateId,
        Object data
    ) {}
}

