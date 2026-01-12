package com.catalyst.payment.infrastructure.persistence.adapter;

import com.catalyst.payment.application.ports.output.PaymentRepository;
import com.catalyst.payment.domain.model.Payment;
import com.catalyst.payment.domain.model.PaymentStatus;
import com.catalyst.payment.infrastructure.persistence.mapper.PaymentMapper;
import com.catalyst.payment.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing PaymentRepository using JPA.
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpaRepository;
    private final PaymentMapper mapper;

    @Override
    public Payment save(Payment payment) {
        var entity = mapper.toJpaEntity(payment);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId) {
        return jpaRepository.findByStripePaymentIntentId(stripePaymentIntentId).map(mapper::toDomain);
    }

    @Override
    public List<Payment> findByInvoiceId(UUID invoiceId) {
        return jpaRepository.findByInvoiceId(invoiceId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        return jpaRepository.findByStatus(status)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

