package com.catalyst.payment.infrastructure.persistence.adapter;

import com.catalyst.payment.application.ports.output.InvoiceRepository;
import com.catalyst.payment.domain.model.Invoice;
import com.catalyst.payment.domain.model.InvoiceStatus;
import com.catalyst.payment.infrastructure.persistence.mapper.InvoiceMapper;
import com.catalyst.payment.infrastructure.persistence.repository.InvoiceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing InvoiceRepository using JPA.
 */
@Repository
@RequiredArgsConstructor
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;
    private final InvoiceMapper mapper;

    @Override
    public Invoice save(Invoice invoice) {
        var entity = mapper.toJpaEntity(invoice);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId) {
        return jpaRepository.findByStripeInvoiceId(stripeInvoiceId).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findBySubscriptionId(UUID subscriptionId) {
        return jpaRepository.findBySubscriptionId(subscriptionId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return jpaRepository.findByStatus(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Invoice> findOverdueInvoices() {
        return jpaRepository.findOverdueInvoices(LocalDateTime.now())
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

