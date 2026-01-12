package com.catalyst.payment.infrastructure.persistence.repository;

import com.catalyst.payment.domain.model.InvoiceStatus;
import com.catalyst.payment.infrastructure.persistence.entity.InvoiceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for invoices.
 */
@Repository
public interface InvoiceJpaRepository extends JpaRepository<InvoiceJpaEntity, UUID> {

    Optional<InvoiceJpaEntity> findByStripeInvoiceId(String stripeInvoiceId);

    List<InvoiceJpaEntity> findBySubscriptionId(UUID subscriptionId);

    List<InvoiceJpaEntity> findByStatus(InvoiceStatus status);

    @Query("SELECT i FROM InvoiceJpaEntity i WHERE i.status = 'OPEN' " +
           "AND i.dueDate < :now")
    List<InvoiceJpaEntity> findOverdueInvoices(@Param("now") LocalDateTime now);
}

