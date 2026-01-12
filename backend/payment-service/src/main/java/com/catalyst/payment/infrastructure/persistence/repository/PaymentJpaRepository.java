package com.catalyst.payment.infrastructure.persistence.repository;

import com.catalyst.payment.domain.model.PaymentStatus;
import com.catalyst.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for payments.
 */
@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

    Optional<PaymentJpaEntity> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<PaymentJpaEntity> findByInvoiceId(UUID invoiceId);

    List<PaymentJpaEntity> findByStatus(PaymentStatus status);
}

