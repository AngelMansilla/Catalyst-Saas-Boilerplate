package com.catalyst.payment.integration;

import com.catalyst.payment.domain.model.BillingCycle;
import com.catalyst.payment.domain.model.SubscriptionTier;
import com.catalyst.payment.infrastructure.stripe.StripeGatewayAdapter;
import com.catalyst.shared.BaseIntegrationTest;
import com.stripe.Stripe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stripe Integration Test")
class StripeIntegrationIT extends BaseIntegrationTest {

    @Container
    static final GenericContainer<?> stripeMock = new GenericContainer<>(
            DockerImageName.parse("stripe/stripe-mock:latest"))
            .withExposedPorts(12111);

    @Autowired
    private StripeGatewayAdapter stripeGatewayAdapter;

    @BeforeEach
    void setUp() {
        // Redirect Stripe API calls to the mock container
        Stripe.overrideApiBase("http://" + stripeMock.getHost() + ":" + stripeMock.getMappedPort(12111));
    }

    @Test
    @DisplayName("Should successfully create a Stripe customer")
    void shouldCreateCustomer() {
        String email = "test@example.com";
        String name = "Test User";

        String customerId = stripeGatewayAdapter.createCustomer(email, name);

        assertThat(customerId).isNotNull();
        assertThat(customerId).startsWith("cus_");
    }

    @Test
    @DisplayName("Should successfully create a checkout session")
    void shouldCreateCheckoutSession() {
        String customerId = "cus_test_123";
        SubscriptionTier tier = SubscriptionTier.PROFESSIONAL;
        BillingCycle cycle = BillingCycle.MONTHLY;
        String successUrl = "http://localhost:3000/success";
        String cancelUrl = "http://localhost:3000/cancel";

        String sessionUrl = stripeGatewayAdapter.createCheckoutSession(customerId, tier, cycle, successUrl, cancelUrl);

        assertThat(sessionUrl).isNotNull();
        assertThat(sessionUrl).contains("checkout.stripe.com");
    }
}
