package com.catalyst.payment.infrastructure.persistence.adapter;

import com.catalyst.payment.application.ports.output.CustomerRepository;
import com.catalyst.payment.domain.model.Customer;
import com.catalyst.payment.infrastructure.persistence.mapper.CustomerMapper;
import com.catalyst.payment.infrastructure.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing CustomerRepository using JPA.
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;
    private final CustomerMapper mapper;

    @Override
    public Customer save(Customer customer) {
        var entity = mapper.toJpaEntity(customer);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByStripeCustomerId(String stripeCustomerId) {
        return jpaRepository.findByStripeCustomerId(stripeCustomerId).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}

