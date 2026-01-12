package com.catalyst.payment.infrastructure.persistence.mapper;

import com.catalyst.payment.domain.model.Customer;
import com.catalyst.payment.domain.valueobject.StripeCustomerId;
import com.catalyst.payment.infrastructure.persistence.entity.CustomerJpaEntity;
import com.catalyst.shared.domain.common.Email;
import org.springframework.stereotype.Component;

/**
 * Mapper between Customer domain entity and JPA entity.
 */
@Component
public class CustomerMapper {

    /**
     * Maps JPA entity to domain entity.
     *
     * @param entity the JPA entity
     * @return the domain entity
     */
    public Customer toDomain(CustomerJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Customer customer = Customer.create(
            entity.getUserId(),
            Email.of(entity.getEmail()),
            entity.getName()
        );

        // Set internal fields using package-private setters
        customer.setId(entity.getId());
        if (entity.getStripeCustomerId() != null) {
            customer.setStripeCustomerId(StripeCustomerId.of(entity.getStripeCustomerId()));
        }
        customer.setCreatedAt(entity.getCreatedAt());
        customer.setUpdatedAt(entity.getUpdatedAt());

        return customer;
    }

    /**
     * Maps domain entity to JPA entity.
     *
     * @param customer the domain entity
     * @return the JPA entity
     */
    public CustomerJpaEntity toJpaEntity(Customer customer) {
        if (customer == null) {
            return null;
        }

        CustomerJpaEntity entity = new CustomerJpaEntity();
        entity.setId(customer.getId());
        entity.setUserId(customer.getUserId());
        entity.setEmail(customer.getEmail().value());
        entity.setName(customer.getName());
        
        if (customer.getStripeCustomerId() != null) {
            entity.setStripeCustomerId(customer.getStripeCustomerId().getValue());
        }
        
        entity.setCreatedAt(customer.getCreatedAt());
        entity.setUpdatedAt(customer.getUpdatedAt());

        return entity;
    }
}

