package com.catalyst.payment.infrastructure.persistence.mapper;

import com.catalyst.payment.domain.model.Payment;
import com.catalyst.payment.domain.valueobject.Money;
import com.catalyst.payment.domain.valueobject.StripePaymentIntentId;
import com.catalyst.payment.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;

/**
 * Mapper between Payment domain entity and JPA entity.
 */
@Component
public class PaymentMapper {

    /**
     * Maps JPA entity to domain entity.
     *
     * @param entity the JPA entity
     * @return the domain entity
     */
    public Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        Currency currency = Currency.getInstance(entity.getCurrency());
        
        Payment payment = Payment.create(
            entity.getInvoiceId(),
            Money.of(entity.getAmount(), currency)
        );

        payment.setId(entity.getId());
        payment.setStatus(entity.getStatus());
        payment.setPaymentMethodType(entity.getPaymentMethodType());
        payment.setFailureReason(entity.getFailureReason());
        
        if (entity.getStripePaymentIntentId() != null) {
            payment.setStripePaymentIntentId(
                StripePaymentIntentId.of(entity.getStripePaymentIntentId())
            );
        }
        
        payment.setCreatedAt(entity.getCreatedAt());
        payment.setUpdatedAt(entity.getUpdatedAt());

        return payment;
    }

    /**
     * Maps domain entity to JPA entity.
     *
     * @param payment the domain entity
     * @return the JPA entity
     */
    public PaymentJpaEntity toJpaEntity(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(payment.getId());
        entity.setInvoiceId(payment.getInvoiceId());
        entity.setStatus(payment.getStatus());
        entity.setAmount(payment.getAmount().getAmount());
        entity.setCurrency(payment.getAmount().getCurrencyCode());
        entity.setPaymentMethodType(payment.getPaymentMethodType());
        entity.setFailureReason(payment.getFailureReason());
        
        if (payment.getStripePaymentIntentId() != null) {
            entity.setStripePaymentIntentId(payment.getStripePaymentIntentId().getValue());
        }
        
        entity.setCreatedAt(payment.getCreatedAt());
        entity.setUpdatedAt(payment.getUpdatedAt());

        return entity;
    }
}

