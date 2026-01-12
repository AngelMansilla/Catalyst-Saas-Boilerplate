package com.catalyst.payment.infrastructure.persistence.repository;

import com.catalyst.payment.domain.model.SubscriptionStatus;
import com.catalyst.payment.infrastructure.persistence.entity.SubscriptionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for subscriptions.
 */
@Repository
public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionJpaEntity, UUID> {

    Optional<SubscriptionJpaEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<SubscriptionJpaEntity> findByCustomerId(UUID customerId);

    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.customerId = :customerId " +
           "AND s.status IN ('TRIAL', 'ACTIVE', 'PAST_DUE')")
    Optional<SubscriptionJpaEntity> findActiveByCustomerId(@Param("customerId") UUID customerId);

    List<SubscriptionJpaEntity> findByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM SubscriptionJpaEntity s WHERE s.status = 'TRIAL' " +
           "AND s.trialEndDate < :now")
    List<SubscriptionJpaEntity> findExpiredTrials(@Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM SubscriptionJpaEntity s WHERE s.customerId = :customerId " +
           "AND s.status IN ('TRIAL', 'ACTIVE', 'PAST_DUE')")
    boolean existsActiveByCustomerId(@Param("customerId") UUID customerId);
}

