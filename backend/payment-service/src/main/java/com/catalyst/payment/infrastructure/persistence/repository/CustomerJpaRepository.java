package com.catalyst.payment.infrastructure.persistence.repository;

import com.catalyst.payment.infrastructure.persistence.entity.CustomerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for customers.
 */
@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

    Optional<CustomerJpaEntity> findByUserId(UUID userId);

    Optional<CustomerJpaEntity> findByStripeCustomerId(String stripeCustomerId);

    Optional<CustomerJpaEntity> findByEmail(String email);

    boolean existsByUserId(UUID userId);
}

