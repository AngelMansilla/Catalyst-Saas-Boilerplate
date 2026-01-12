package com.catalyst.payment.domain.model;

import com.catalyst.payment.domain.exception.InvalidSubscriptionStateException;
import com.catalyst.payment.domain.valueobject.StripeSubscriptionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Subscription aggregate root and state machine.
 */
@DisplayName("Subscription")
class SubscriptionTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final int TRIAL_DAYS = 14;

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("startTrial creates subscription in TRIAL status")
        void startTrialCreatesSubscriptionInTrialStatus() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);

            assertThat(subscription.getId()).isNotNull();
            assertThat(subscription.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.TRIAL);
            assertThat(subscription.getTier()).isEqualTo(SubscriptionTier.PROFESSIONAL);
            assertThat(subscription.getTrialEndDate()).isAfter(LocalDateTime.now());
            assertThat(subscription.isInTrial()).isTrue();
        }

        @Test
        @DisplayName("startTrial throws exception for null customerId")
        void startTrialThrowsForNullCustomerId() {
            assertThatThrownBy(() -> Subscription.startTrial(null, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer ID");
        }

        @Test
        @DisplayName("startTrial throws exception for null tier")
        void startTrialThrowsForNullTier() {
            assertThatThrownBy(() -> Subscription.startTrial(CUSTOMER_ID, null, TRIAL_DAYS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tier");
        }

        @Test
        @DisplayName("startTrial throws exception for non-positive trial days")
        void startTrialThrowsForNonPositiveTrialDays() {
            assertThatThrownBy(() -> Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Trial days");
        }
    }

    @Nested
    @DisplayName("State Transitions")
    class StateTransitions {

        @Test
        @DisplayName("TRIAL -> ACTIVE via activate()")
        void trialToActiveTransition() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            StripeSubscriptionId stripeId = StripeSubscriptionId.of("sub_123456");

            subscription.activate(stripeId, BillingCycle.MONTHLY);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.getStripeSubscriptionId()).isEqualTo(stripeId);
            assertThat(subscription.getBillingCycle()).isEqualTo(BillingCycle.MONTHLY);
            assertThat(subscription.getTrialEndDate()).isNull();
            assertThat(subscription.getCurrentPeriodEnd()).isNotNull();
            assertThat(subscription.isActive()).isTrue();
            assertThat(subscription.isInTrial()).isFalse();
        }

        @Test
        @DisplayName("TRIAL -> CANCELED via cancel()")
        void trialToCanceledTransition() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);

            subscription.cancel(CancellationReason.USER_REQUESTED, true);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
            assertThat(subscription.getCancellationReason()).isEqualTo(CancellationReason.USER_REQUESTED);
            assertThat(subscription.getCanceledAt()).isNotNull();
            assertThat(subscription.isCanceled()).isTrue();
        }

        @Test
        @DisplayName("TRIAL -> EXPIRED via expireTrial()")
        void trialToExpiredTransition() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            // Manually set trial end to past
            subscription.setTrialEndDate(LocalDateTime.now().minusDays(1));

            subscription.expireTrial();

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.EXPIRED);
        }

        @Test
        @DisplayName("ACTIVE -> PAST_DUE via markPastDue()")
        void activeToPastDueTransition() {
            Subscription subscription = createActiveSubscription();

            subscription.markPastDue();

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PAST_DUE);
            assertThat(subscription.isPastDue()).isTrue();
        }

        @Test
        @DisplayName("ACTIVE -> CANCELED via cancel()")
        void activeToCanceledTransition() {
            Subscription subscription = createActiveSubscription();

            subscription.cancel(CancellationReason.USER_REQUESTED, false);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        }

        @Test
        @DisplayName("PAST_DUE -> ACTIVE via recordPaymentSuccess()")
        void pastDueToActiveTransition() {
            Subscription subscription = createActiveSubscription();
            subscription.markPastDue();

            subscription.recordPaymentSuccess();

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("PAST_DUE -> CANCELED via cancel()")
        void pastDueToCanceledTransition() {
            Subscription subscription = createActiveSubscription();
            subscription.markPastDue();

            subscription.cancel(CancellationReason.PAYMENT_FAILED, true);

            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELED);
        }
    }

    @Nested
    @DisplayName("Invalid State Transitions")
    class InvalidStateTransitions {

        @Test
        @DisplayName("TRIAL cannot transition to PAST_DUE")
        void trialCannotTransitionToPastDue() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);

            assertThatThrownBy(subscription::markPastDue)
                .isInstanceOf(InvalidSubscriptionStateException.class)
                .hasMessageContaining("TRIAL")
                .hasMessageContaining("PAST_DUE");
        }

        @Test
        @DisplayName("CANCELED can be reactivated via activate() for subscription reactivation")
        void canceledCanBeReactivated() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            subscription.cancel(CancellationReason.USER_REQUESTED, true);

            // CANCELED allows transition to ACTIVE for reactivation
            subscription.activate(StripeSubscriptionId.of("sub_123"), BillingCycle.MONTHLY);
            
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(subscription.canReactivate()).isFalse(); // No longer reactivatable
        }

        @Test
        @DisplayName("EXPIRED subscription expireTrial fails")
        void expiredSubscriptionExpireTrialFails() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            subscription.setTrialEndDate(LocalDateTime.now().minusDays(1));
            subscription.expireTrial();

            assertThatThrownBy(subscription::expireTrial)
                .isInstanceOf(InvalidSubscriptionStateException.class)
                .hasMessageContaining("TRIAL");
        }
    }

    @Nested
    @DisplayName("Business Logic")
    class BusinessLogic {

        @Test
        @DisplayName("isInTrial returns true only during valid trial period")
        void isInTrialReturnsCorrectly() {
            Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            
            assertThat(subscription.isInTrial()).isTrue();

            // After expiration
            subscription.setTrialEndDate(LocalDateTime.now().minusDays(1));
            assertThat(subscription.isInTrial()).isFalse();
        }

        @Test
        @DisplayName("canReactivate returns true for CANCELED and EXPIRED")
        void canReactivateReturnsCorrectly() {
            Subscription canceledSub = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
            canceledSub.cancel(CancellationReason.USER_REQUESTED, true);
            assertThat(canceledSub.canReactivate()).isTrue();

            Subscription expiredSub = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.CLINIC, TRIAL_DAYS);
            expiredSub.setTrialEndDate(LocalDateTime.now().minusDays(1));
            expiredSub.expireTrial();
            assertThat(expiredSub.canReactivate()).isTrue();

            Subscription activeSub = createActiveSubscription();
            assertThat(activeSub.canReactivate()).isFalse();
        }

        @Test
        @DisplayName("updatePeriod validates dates")
        void updatePeriodValidatesDates() {
            Subscription subscription = createActiveSubscription();
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusMonths(1);

            subscription.updatePeriod(start, end);

            assertThat(subscription.getCurrentPeriodStart()).isEqualTo(start);
            assertThat(subscription.getCurrentPeriodEnd()).isEqualTo(end);
        }

        @Test
        @DisplayName("updatePeriod throws for null dates")
        void updatePeriodThrowsForNullDates() {
            Subscription subscription = createActiveSubscription();

            assertThatThrownBy(() -> subscription.updatePeriod(null, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);

            assertThatThrownBy(() -> subscription.updatePeriod(LocalDateTime.now(), null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("updatePeriod throws when start is after end")
        void updatePeriodThrowsWhenStartAfterEnd() {
            Subscription subscription = createActiveSubscription();
            LocalDateTime start = LocalDateTime.now().plusMonths(1);
            LocalDateTime end = LocalDateTime.now();

            assertThatThrownBy(() -> subscription.updatePeriod(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before");
        }
    }

    @Nested
    @DisplayName("Subscription Status")
    class SubscriptionStatusTests {

        @Test
        @DisplayName("TRIAL allows transition to ACTIVE, CANCELED, EXPIRED")
        void trialAllowedTransitions() {
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.ACTIVE)).isTrue();
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.CANCELED)).isTrue();
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.EXPIRED)).isTrue();
            assertThat(SubscriptionStatus.TRIAL.canTransitionTo(SubscriptionStatus.PAST_DUE)).isFalse();
        }

        @Test
        @DisplayName("ACTIVE allows transition to PAST_DUE, CANCELED")
        void activeAllowedTransitions() {
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.PAST_DUE)).isTrue();
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.CANCELED)).isTrue();
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.TRIAL)).isFalse();
            assertThat(SubscriptionStatus.ACTIVE.canTransitionTo(SubscriptionStatus.EXPIRED)).isFalse();
        }

        @Test
        @DisplayName("PAST_DUE allows transition to ACTIVE, CANCELED")
        void pastDueAllowedTransitions() {
            assertThat(SubscriptionStatus.PAST_DUE.canTransitionTo(SubscriptionStatus.ACTIVE)).isTrue();
            assertThat(SubscriptionStatus.PAST_DUE.canTransitionTo(SubscriptionStatus.CANCELED)).isTrue();
            assertThat(SubscriptionStatus.PAST_DUE.canTransitionTo(SubscriptionStatus.TRIAL)).isFalse();
        }
    }

    // Helper method to create an active subscription
    private Subscription createActiveSubscription() {
        Subscription subscription = Subscription.startTrial(CUSTOMER_ID, SubscriptionTier.PROFESSIONAL, TRIAL_DAYS);
        subscription.activate(StripeSubscriptionId.of("sub_test123"), BillingCycle.MONTHLY);
        return subscription;
    }
}

