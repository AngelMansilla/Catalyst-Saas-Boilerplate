package com.catalyst.payment.infrastructure.web;

import com.catalyst.payment.application.ports.input.ProcessWebhookUseCase;
import com.catalyst.payment.application.ports.input.ProcessWebhookUseCase.WebhookResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Stripe webhook processing.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Stripe webhook processing")
public class WebhookController {

    private static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";

    private final ProcessWebhookUseCase processWebhookUseCase;

    @PostMapping("/stripe")
    @Operation(summary = "Process Stripe webhook", description = "Processes incoming Stripe webhook events")
    public ResponseEntity<WebhookResponse> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(STRIPE_SIGNATURE_HEADER) String signature) {
        
        log.info("Received Stripe webhook");
        
        WebhookResult result = processWebhookUseCase.execute(payload, signature);

        if (result.success()) {
            log.info("Webhook processed successfully: {} ({})", result.eventId(), result.eventType());
            return ResponseEntity.ok(new WebhookResponse(true, result.message()));
        } else {
            log.warn("Webhook processing failed: {}", result.message());
            return ResponseEntity.badRequest().body(new WebhookResponse(false, result.message()));
        }
    }

    /**
     * Response DTO for webhook processing.
     */
    public record WebhookResponse(boolean success, String message) {}
}

