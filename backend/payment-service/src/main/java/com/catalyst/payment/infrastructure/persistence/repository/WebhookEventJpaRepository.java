package com.catalyst.payment.infrastructure.persistence.repository;

import com.catalyst.payment.infrastructure.persistence.entity.WebhookEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for webhook events.
 */
@Repository
public interface WebhookEventJpaRepository extends JpaRepository<WebhookEventJpaEntity, UUID> {

    boolean existsByStripeEventId(String stripeEventId);

    Optional<WebhookEventJpaEntity> findByStripeEventId(String stripeEventId);
}

