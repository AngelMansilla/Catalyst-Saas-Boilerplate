package com.catalyst.payment.integration;

import com.catalyst.shared.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Stripe Webhook Integration Test")
class StripeWebhookIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 400 when webhook signature is missing")
    void shouldReturn400WhenSignatureMissing() throws Exception {
        String payload = "{\"id\": \"evt_test\", \"type\": \"checkout.session.completed\"}";

        mockMvc.perform(post("/api/v1/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when webhook signature is invalid")
    void shouldReturn400WhenSignatureInvalid() throws Exception {
        String payload = "{\"id\": \"evt_test\", \"type\": \"checkout.session.completed\"}";

        mockMvc.perform(post("/api/v1/webhooks/stripe")
                .header("Stripe-Signature", "invalid_sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }
}
