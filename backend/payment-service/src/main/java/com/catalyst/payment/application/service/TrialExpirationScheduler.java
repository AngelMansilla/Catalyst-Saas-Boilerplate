package com.catalyst.payment.application.service;

import com.catalyst.payment.application.ports.output.CustomerRepository;
import com.catalyst.payment.application.ports.output.EventPublisher;
import com.catalyst.payment.application.ports.output.SubscriptionRepository;
import com.catalyst.payment.domain.event.TrialExpired;
import com.catalyst.payment.domain.exception.PaymentException;
import com.catalyst.payment.domain.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Scheduled service that handles trial expiration.
 * Runs periodically to check for expired trials and update their status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrialExpirationScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final EventPublisher eventPublisher;

    /**
     * Checks for expired trials every hour and marks them as expired.
     */
    @Scheduled(cron = "${payment.trial.expiration-cron:0 0 * * * *}")
    @Transactional
    public void processExpiredTrials() {
        log.info("Starting trial expiration check");

        List<Subscription> expiredTrials = subscriptionRepository.findExpiredTrials();
        
        if (expiredTrials.isEmpty()) {
            log.debug("No expired trials found");
            return;
        }

        log.info("Found {} expired trials to process", expiredTrials.size());

        int processed = 0;
        int errors = 0;

        for (Subscription subscription : expiredTrials) {
            try {
                processExpiredTrial(subscription);
                processed++;
            } catch (Exception e) {
                log.error("Error processing expired trial {}: {}", 
                    subscription.getId(), e.getMessage(), e);
                errors++;
            }
        }

        log.info("Trial expiration check completed: {} processed, {} errors", processed, errors);
    }

    /**
     * Processes a single expired trial subscription.
     */
    private void processExpiredTrial(Subscription subscription) {
        log.debug("Processing expired trial: {}", subscription.getId());

        // Mark as expired
        subscription.expireTrial();
        subscriptionRepository.save(subscription);

        // Get customer for event
        var customer = customerRepository.findById(subscription.getCustomerId())
            .orElseThrow(() -> new PaymentException("Customer not found"));

        // Publish event
        TrialExpired event = TrialExpired.of(subscription.getId(), customer.getUserId());
        eventPublisher.publish(event, UUID.randomUUID().toString());

        log.info("Trial expired for subscription: {}", subscription.getId());
    }

    /**
     * Manually expire a specific subscription's trial.
     * Used for testing or administrative purposes.
     */
    @Transactional
    public void expireTrial(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new PaymentException("Subscription not found: " + subscriptionId));

        if (!subscription.isInTrial()) {
            throw new PaymentException("Subscription is not in trial status");
        }

        processExpiredTrial(subscription);
    }
}

