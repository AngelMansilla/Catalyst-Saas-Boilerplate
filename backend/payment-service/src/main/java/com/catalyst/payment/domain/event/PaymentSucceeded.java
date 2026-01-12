package com.catalyst.payment.domain.event;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain event emitted when a payment succeeds.
 */
@Getter
@Builder
public class PaymentSucceeded implements DomainEvent {
    
    private final UUID eventId;
    private final Instant timestamp;
    private final UUID paymentId;
    private final UUID subscriptionId;
    private final UUID invoiceId;
    private final BigDecimal amount;
    private final String currency;
    private final String stripePaymentIntentId;

    @Override
    public String getEventType() {
        return "PaymentSucceeded";
    }

    @Override
    public UUID getAggregateId() {
        return paymentId;
    }

    public static PaymentSucceeded of(UUID paymentId, UUID subscriptionId, UUID invoiceId,
                                     BigDecimal amount, String currency, String stripePaymentIntentId) {
        return PaymentSucceeded.builder()
            .eventId(UUID.randomUUID())
            .timestamp(Instant.now())
            .paymentId(paymentId)
            .subscriptionId(subscriptionId)
            .invoiceId(invoiceId)
            .amount(amount)
            .currency(currency)
            .stripePaymentIntentId(stripePaymentIntentId)
            .build();
    }

    /**
     * Factory method for webhook processing (subscription-level event).
     */
    public static PaymentSucceeded of(UUID subscriptionId, UUID userId, 
                                     BigDecimal amount, String currency, String stripeInvoiceId) {
        return PaymentSucceeded.builder()
            .eventId(UUID.randomUUID())
            .timestamp(Instant.now())
            .paymentId(UUID.randomUUID())
            .subscriptionId(subscriptionId)
            .amount(amount)
            .currency(currency)
            .stripePaymentIntentId(stripeInvoiceId)
            .build();
    }
}

