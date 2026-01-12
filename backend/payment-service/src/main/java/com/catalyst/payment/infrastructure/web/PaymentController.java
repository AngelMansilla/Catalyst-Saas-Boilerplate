package com.catalyst.payment.infrastructure.web;

import com.catalyst.payment.application.dto.*;
import com.catalyst.payment.application.ports.input.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for payment and subscription operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment and subscription management")
public class PaymentController {

    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final CancelSubscriptionUseCase cancelSubscriptionUseCase;
    private final GetCustomerPortalUrlUseCase getCustomerPortalUrlUseCase;

    @PostMapping("/subscriptions/checkout")
    @Operation(summary = "Create checkout session", description = "Creates a Stripe checkout session for a new subscription")
    public ResponseEntity<CreateSubscriptionResponse> createCheckoutSession(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        log.info("Creating checkout session for user: {}", request.userId());
        var response = createSubscriptionUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subscriptions/{subscriptionId}")
    @Operation(summary = "Get subscription", description = "Gets a subscription by ID")
    public ResponseEntity<SubscriptionDto> getSubscription(@PathVariable UUID subscriptionId) {
        log.info("Getting subscription: {}", subscriptionId);
        var subscription = getSubscriptionUseCase.getById(subscriptionId);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/subscriptions/user/{userId}")
    @Operation(summary = "Get user subscriptions", description = "Gets all subscriptions for a user")
    public ResponseEntity<List<SubscriptionDto>> getUserSubscriptions(@PathVariable UUID userId) {
        log.info("Getting subscriptions for user: {}", userId);
        var subscriptions = getSubscriptionUseCase.getAllByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/subscriptions/user/{userId}/active")
    @Operation(summary = "Get active subscription", description = "Gets the active subscription for a user")
    public ResponseEntity<SubscriptionDto> getActiveSubscription(@PathVariable UUID userId) {
        log.info("Getting active subscription for user: {}", userId);
        var subscription = getSubscriptionUseCase.getActiveByUserId(userId);
        if (subscription == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/subscriptions/user/{userId}/has-active")
    @Operation(summary = "Check active subscription", description = "Checks if a user has an active subscription")
    public ResponseEntity<Boolean> hasActiveSubscription(@PathVariable UUID userId) {
        log.info("Checking active subscription for user: {}", userId);
        var hasActive = getSubscriptionUseCase.hasActiveSubscription(userId);
        return ResponseEntity.ok(hasActive);
    }

    @PostMapping("/subscriptions/{subscriptionId}/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancels a subscription")
    public ResponseEntity<SubscriptionDto> cancelSubscription(
            @PathVariable UUID subscriptionId,
            @Valid @RequestBody CancelSubscriptionRequest request) {
        log.info("Canceling subscription: {}", subscriptionId);
        
        // Ensure the path variable matches the request body
        var cancelRequest = CancelSubscriptionRequest.builder()
            .subscriptionId(subscriptionId)
            .reason(request.reason())
            .immediate(request.immediate())
            .build();
            
        var subscription = cancelSubscriptionUseCase.execute(cancelRequest);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping("/portal/{userId}")
    @Operation(summary = "Get customer portal URL", description = "Gets the Stripe customer portal URL for a user")
    public ResponseEntity<PortalUrlResponse> getCustomerPortalUrl(
            @PathVariable UUID userId,
            @RequestParam String returnUrl) {
        log.info("Getting customer portal URL for user: {}", userId);
        var portalUrl = getCustomerPortalUrlUseCase.execute(userId, returnUrl);
        return ResponseEntity.ok(new PortalUrlResponse(portalUrl));
    }

    /**
     * Response DTO for customer portal URL.
     */
    public record PortalUrlResponse(String url) {}
}

