package com.catalyst.user.infrastructure.kafka;

import com.catalyst.user.application.ports.output.EventPublisher;
import com.catalyst.user.domain.event.PasswordResetCompleted;
import com.catalyst.user.domain.event.PasswordResetRequested;
import com.catalyst.user.domain.event.UserDeleted;
import com.catalyst.user.domain.event.UserLoggedIn;
import com.catalyst.user.domain.event.UserRegistered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of EventPublisher used when Kafka is not available.
 * This allows the service to start and function even without Kafka configured.
 * 
 * <p>
 * Events are logged but not published to any message queue.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
@ConditionalOnMissingBean(KafkaTemplate.class)
public class NoOpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisher.class);

    @Override
    public void publish(UserRegistered event) {
        log.debug("Event publishing disabled (Kafka not available): UserRegistered for user {}",
                event.userId());
    }

    @Override
    public void publish(UserLoggedIn event) {
        log.debug("Event publishing disabled (Kafka not available): UserLoggedIn for user {}",
                event.userId());
    }

    @Override
    public void publish(PasswordResetRequested event) {
        log.debug("Event publishing disabled (Kafka not available): PasswordResetRequested for user {}",
                event.userId());
    }

    @Override
    public void publish(PasswordResetCompleted event) {
        log.debug("Event publishing disabled (Kafka not available): PasswordResetCompleted for user {}",
                event.userId());
    }

    @Override
    public void publish(UserDeleted event) {
        log.debug("Event publishing disabled (Kafka not available): UserDeleted for user {}",
                event.userId());
    }
}
