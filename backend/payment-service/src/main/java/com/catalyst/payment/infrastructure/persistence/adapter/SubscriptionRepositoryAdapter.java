package com.catalyst.payment.infrastructure.persistence.adapter;

import com.catalyst.payment.application.ports.output.SubscriptionRepository;
import com.catalyst.payment.domain.model.Subscription;
import com.catalyst.payment.domain.model.SubscriptionStatus;
import com.catalyst.payment.infrastructure.persistence.mapper.SubscriptionMapper;
import com.catalyst.payment.infrastructure.persistence.repository.SubscriptionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing SubscriptionRepository using JPA.
 */
@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;
    private final SubscriptionMapper mapper;

    @Override
    public Subscription save(Subscription subscription) {
        var entity = mapper.toJpaEntity(subscription);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return jpaRepository.findByStripeSubscriptionId(stripeSubscriptionId).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerId(customerId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<Subscription> findActiveByCustomerId(UUID customerId) {
        return jpaRepository.findActiveByCustomerId(customerId).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findByStatus(SubscriptionStatus status) {
        return jpaRepository.findByStatus(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Subscription> findExpiredTrials() {
        return jpaRepository.findExpiredTrials(LocalDateTime.now())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsActiveByCustomerId(UUID customerId) {
        return jpaRepository.existsActiveByCustomerId(customerId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

