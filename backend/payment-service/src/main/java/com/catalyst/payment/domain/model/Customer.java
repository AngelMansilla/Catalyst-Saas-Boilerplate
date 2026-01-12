package com.catalyst.payment.domain.model;

import com.catalyst.payment.domain.valueobject.StripeCustomerId;
import com.catalyst.shared.domain.common.Email;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Customer entity representing a payment customer.
 * Framework-agnostic domain entity.
 */
@Getter
public class Customer {
    
    private UUID id;
    private UUID userId;
    private StripeCustomerId stripeCustomerId;
    private Email email;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Private constructor
    private Customer() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a new customer.
     *
     * @param userId the user ID
     * @param email the customer email
     * @param name the customer name
     * @return a new customer
     */
    public static Customer create(UUID userId, Email email, String name) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        Customer customer = new Customer();
        customer.userId = userId;
        customer.email = email;
        customer.name = name;
        
        return customer;
    }

    /**
     * Associates a Stripe customer ID with this customer.
     *
     * @param stripeCustomerId the Stripe customer ID
     */
    public void associateStripeCustomer(StripeCustomerId stripeCustomerId) {
        if (stripeCustomerId == null) {
            throw new IllegalArgumentException("Stripe Customer ID cannot be null");
        }
        
        this.stripeCustomerId = stripeCustomerId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates customer information.
     *
     * @param email the new email
     * @param name the new name
     */
    public void updateInfo(Email email, String name) {
        if (email != null) {
            this.email = email;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Setters for persistence layer (reconstitution)
    public void setId(UUID id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setStripeCustomerId(StripeCustomerId stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

