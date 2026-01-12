package com.catalyst.payment.application.service;

import com.catalyst.payment.application.ports.output.*;
import com.catalyst.payment.domain.event.*;
import com.catalyst.payment.domain.exception.PaymentException;
import com.catalyst.payment.domain.exception.SubscriptionNotFoundException;
import com.catalyst.payment.domain.model.*;
import com.catalyst.payment.domain.valueobject.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Currency;
import java.util.UUID;

/**
 * Handles Stripe webhook events with idempotency and proper event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventHandler {

    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${payment.trial.duration-days:14}")
    private int trialDurationDays;

    /**
     * Handles checkout.session.completed event.
     * Creates customer and subscription when checkout is successful.
     */
    @Transactional
    public void handleCheckoutSessionCompleted(Object eventData) {
        try {
            JsonNode data = objectMapper.valueToTree(eventData);
            
            String stripeCustomerId = data.path("customer").asText();
            String stripeSubscriptionId = data.path("subscription").asText();
            String customerEmail = data.path("customer_email").asText();
            String mode = data.path("mode").asText();
            
            // Only handle subscription mode
            if (!"subscription".equals(mode)) {
                log.info("Ignoring checkout session with mode: {}", mode);
                return;
            }

            log.info("Processing checkout.session.completed for customer: {}", stripeCustomerId);

            // Get or create customer
            Customer customer = customerRepository.findByStripeCustomerId(stripeCustomerId)
                .orElseGet(() -> createCustomerFromCheckout(stripeCustomerId, customerEmail));

            // Get subscription metadata
            JsonNode metadata = data.path("subscription_data").path("metadata");
            SubscriptionTier tier = SubscriptionTier.valueOf(
                metadata.path("tier").asText("PROFESSIONAL")
            );
            BillingCycle billingCycle = BillingCycle.valueOf(
                metadata.path("billing_cycle").asText("MONTHLY")
            );

            // Create subscription
            Subscription subscription = Subscription.startTrial(
                customer.getId(), 
                tier, 
                trialDurationDays
            );
            
            // Activate immediately since payment was successful
            subscription.activate(
                StripeSubscriptionId.of(stripeSubscriptionId),
                billingCycle
            );
            
            subscription = subscriptionRepository.save(subscription);

            // Publish events
            String correlationId = UUID.randomUUID().toString();
            
            SubscriptionCreated createdEvent = SubscriptionCreated.of(
                subscription.getId(),
                customer.getUserId(),
                tier,
                null // No trial since activated immediately
            );
            eventPublisher.publish(createdEvent, correlationId);

            SubscriptionActivated activatedEvent = SubscriptionActivated.of(
                subscription.getId(),
                customer.getUserId(),
                tier,
                billingCycle,
                stripeSubscriptionId,
                subscription.getCurrentPeriodEnd()
            );
            eventPublisher.publish(activatedEvent, correlationId);

            log.info("Checkout completed: subscription {} created for customer {}", 
                subscription.getId(), customer.getId());

        } catch (Exception e) {
            log.error("Error handling checkout.session.completed", e);
            throw new PaymentException("WEBHOOK.CHECKOUT_FAILED", 
                "Failed to process checkout session: " + e.getMessage(), e);
        }
    }

    /**
     * Handles invoice.paid event.
     * Updates subscription status and creates invoice record.
     */
    @Transactional
    public void handleInvoicePaid(Object eventData) {
        try {
            JsonNode data = objectMapper.valueToTree(eventData);
            
            String stripeInvoiceId = data.path("id").asText();
            String stripeSubscriptionId = data.path("subscription").asText();
            String stripeCustomerId = data.path("customer").asText();
            long amountPaidCents = data.path("amount_paid").asLong();
            String currency = data.path("currency").asText().toUpperCase();
            String invoicePdfUrl = data.path("invoice_pdf").asText(null);
            String hostedInvoiceUrl = data.path("hosted_invoice_url").asText(null);

            log.info("Processing invoice.paid: {}", stripeInvoiceId);

            // Find subscription
            Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscriptionId)
                .orElse(null);

            if (subscription == null) {
                log.warn("Subscription not found for invoice: {}", stripeSubscriptionId);
                return;
            }

            // Capture subscription ID before mutation
            final UUID subscriptionId = subscription.getId();

            // Record payment success (reactivates if past due)
            subscription.recordPaymentSuccess();
            final Subscription savedSubscription = subscriptionRepository.save(subscription);

            // Create or update invoice
            BigDecimal amountPaid = BigDecimal.valueOf(amountPaidCents).divide(BigDecimal.valueOf(100));
            Money money = Money.of(amountPaid, Currency.getInstance(currency));

            Invoice invoice = invoiceRepository.findByStripeInvoiceId(stripeInvoiceId)
                .orElseGet(() -> Invoice.create(subscriptionId, money));

            invoice.setStripeInvoiceId(StripeInvoiceId.of(stripeInvoiceId));
            invoice.markAsPaid(money, LocalDateTime.now());
            invoice.updateUrls(invoicePdfUrl, hostedInvoiceUrl);
            invoiceRepository.save(invoice);

            // Publish event
            Customer customer = customerRepository.findById(savedSubscription.getCustomerId())
                .orElseThrow(() -> new PaymentException("Customer not found"));

            PaymentSucceeded event = PaymentSucceeded.of(
                savedSubscription.getId(),
                customer.getUserId(),
                amountPaid,
                currency,
                stripeInvoiceId
            );
            eventPublisher.publish(event, UUID.randomUUID().toString());

            log.info("Invoice paid: {} for subscription {}", stripeInvoiceId, subscription.getId());

        } catch (Exception e) {
            log.error("Error handling invoice.paid", e);
            throw new PaymentException("WEBHOOK.INVOICE_PAID_FAILED", 
                "Failed to process invoice.paid: " + e.getMessage(), e);
        }
    }

    /**
     * Handles invoice.payment_failed event.
     * Marks subscription as past due.
     */
    @Transactional
    public void handleInvoicePaymentFailed(Object eventData) {
        try {
            JsonNode data = objectMapper.valueToTree(eventData);
            
            String stripeInvoiceId = data.path("id").asText();
            String stripeSubscriptionId = data.path("subscription").asText();
            String failureMessage = data.path("last_payment_error").path("message").asText("Payment failed");

            log.info("Processing invoice.payment_failed: {}", stripeInvoiceId);

            // Find subscription
            Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscriptionId)
                .orElse(null);

            if (subscription == null) {
                log.warn("Subscription not found for failed invoice: {}", stripeSubscriptionId);
                return;
            }

            // Mark as past due
            if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                subscription.markPastDue();
                subscription = subscriptionRepository.save(subscription);

                // Publish event
                Customer customer = customerRepository.findById(subscription.getCustomerId())
                    .orElseThrow(() -> new PaymentException("Customer not found"));

                PaymentFailed event = PaymentFailed.of(
                    subscription.getId(),
                    customer.getUserId(),
                    failureMessage,
                    stripeInvoiceId
                );
                eventPublisher.publish(event, UUID.randomUUID().toString());

                log.info("Subscription {} marked as past due", subscription.getId());
            }

        } catch (Exception e) {
            log.error("Error handling invoice.payment_failed", e);
            throw new PaymentException("WEBHOOK.PAYMENT_FAILED_ERROR", 
                "Failed to process payment_failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handles customer.subscription.deleted event.
     * Cancels the subscription.
     */
    @Transactional
    public void handleSubscriptionDeleted(Object eventData) {
        try {
            JsonNode data = objectMapper.valueToTree(eventData);
            
            String stripeSubscriptionId = data.path("id").asText();
            String cancellationReason = data.path("cancellation_details")
                .path("reason").asText("other");

            log.info("Processing customer.subscription.deleted: {}", stripeSubscriptionId);

            Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscriptionId)
                .orElse(null);

            if (subscription == null) {
                log.warn("Subscription not found for deletion: {}", stripeSubscriptionId);
                return;
            }

            // Cancel if not already canceled
            if (subscription.getStatus() != SubscriptionStatus.CANCELED) {
                CancellationReason reason = mapCancellationReason(cancellationReason);
                subscription.cancel(reason, true);
                subscription = subscriptionRepository.save(subscription);

                // Publish event
                Customer customer = customerRepository.findById(subscription.getCustomerId())
                    .orElseThrow(() -> new PaymentException("Customer not found"));

                SubscriptionCanceled event = SubscriptionCanceled.of(
                    subscription.getId(),
                    customer.getUserId(),
                    reason,
                    true,
                    LocalDateTime.now()
                );
                eventPublisher.publish(event, UUID.randomUUID().toString());

                log.info("Subscription {} canceled via webhook", subscription.getId());
            }

        } catch (Exception e) {
            log.error("Error handling customer.subscription.deleted", e);
            throw new PaymentException("WEBHOOK.SUBSCRIPTION_DELETED_FAILED", 
                "Failed to process subscription.deleted: " + e.getMessage(), e);
        }
    }

    /**
     * Handles customer.subscription.updated event.
     * Updates subscription status based on Stripe status.
     */
    @Transactional
    public void handleSubscriptionUpdated(Object eventData) {
        try {
            JsonNode data = objectMapper.valueToTree(eventData);
            
            String stripeSubscriptionId = data.path("id").asText();
            String status = data.path("status").asText();
            long currentPeriodEnd = data.path("current_period_end").asLong();
            boolean cancelAtPeriodEnd = data.path("cancel_at_period_end").asBoolean();

            log.info("Processing customer.subscription.updated: {} (status: {})", 
                stripeSubscriptionId, status);

            Subscription subscription = subscriptionRepository
                .findByStripeSubscriptionId(stripeSubscriptionId)
                .orElse(null);

            if (subscription == null) {
                log.warn("Subscription not found for update: {}", stripeSubscriptionId);
                return;
            }

            // Update period end
            LocalDateTime periodEnd = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(currentPeriodEnd), 
                ZoneId.systemDefault()
            );
            subscription.setCurrentPeriodEnd(periodEnd);

            // Update status based on Stripe status
            switch (status) {
                case "active" -> {
                    if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
                        subscription.recordPaymentSuccess();
                    }
                }
                case "past_due" -> {
                    if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
                        subscription.markPastDue();
                    }
                }
                case "canceled" -> {
                    if (subscription.getStatus() != SubscriptionStatus.CANCELED) {
                        subscription.cancel(CancellationReason.OTHER, true);
                    }
                }
                case "unpaid" -> {
                    subscription.markPastDue();
                }
            }

            subscriptionRepository.save(subscription);
            log.info("Subscription {} updated to status: {}", subscription.getId(), status);

        } catch (Exception e) {
            log.error("Error handling customer.subscription.updated", e);
            throw new PaymentException("WEBHOOK.SUBSCRIPTION_UPDATED_FAILED", 
                "Failed to process subscription.updated: " + e.getMessage(), e);
        }
    }

    // ==================== Helper Methods ====================

    private Customer createCustomerFromCheckout(String stripeCustomerId, String email) {
        // Create customer without userId (will need to be linked later)
        Customer customer = Customer.create(
            UUID.randomUUID(), // Temporary userId
            com.catalyst.shared.domain.common.Email.of(email),
            null
        );
        customer.associateStripeCustomer(StripeCustomerId.of(stripeCustomerId));
        return customerRepository.save(customer);
    }

    private CancellationReason mapCancellationReason(String stripeReason) {
        return switch (stripeReason) {
            case "customer" -> CancellationReason.USER_REQUESTED;
            case "payment_failure" -> CancellationReason.PAYMENT_FAILED;
            case "subscription_change" -> CancellationReason.OTHER;
            default -> CancellationReason.OTHER;
        };
    }
}

