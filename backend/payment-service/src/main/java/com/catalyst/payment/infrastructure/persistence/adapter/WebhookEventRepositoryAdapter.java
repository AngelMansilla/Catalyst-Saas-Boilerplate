package com.catalyst.payment.infrastructure.persistence.adapter;

import com.catalyst.payment.application.ports.output.WebhookEventRepository;
import com.catalyst.payment.infrastructure.persistence.entity.WebhookEventJpaEntity;
import com.catalyst.payment.infrastructure.persistence.repository.WebhookEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing WebhookEventRepository using JPA.
 */
@Repository
@RequiredArgsConstructor
public class WebhookEventRepositoryAdapter implements WebhookEventRepository {

    private final WebhookEventJpaRepository jpaRepository;

    @Override
    public boolean existsByStripeEventId(String stripeEventId) {
        return jpaRepository.existsByStripeEventId(stripeEventId);
    }

    @Override
    public UUID recordProcessedEvent(String stripeEventId, String eventType, String payload) {
        var entity = new WebhookEventJpaEntity(stripeEventId, eventType, payload);
        var saved = jpaRepository.save(entity);
        return saved.getId();
    }

    @Override
    public Optional<String> findPayloadByStripeEventId(String stripeEventId) {
        return jpaRepository.findByStripeEventId(stripeEventId)
            .map(WebhookEventJpaEntity::getPayload);
    }
}

