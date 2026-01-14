package com.catalyst.notification.infrastructure.kafka;

import com.catalyst.notification.application.dto.SendNotificationRequest;
import com.catalyst.notification.application.ports.input.SendNotificationUseCase;
import com.catalyst.notification.domain.valueobject.NotificationType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer for payment-service events.
 * Handles subscription and payment events.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class PaymentEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    
    private final SendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;
    
    @Value("${email.base-url:http://localhost:3000}")
    private String baseUrl;
    
    public PaymentEventConsumer(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.objectMapper = new ObjectMapper();
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.subscription-created:payment.subscription.created}")
    public void handleSubscriptionCreated(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received SubscriptionCreated event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode data = envelope.get("data");
            
            String userId = data.get("userId").asText();
            String planName = data.get("planName").asText();
            String trialEndDate = data.has("trialEndDate") ? data.get("trialEndDate").asText() : null;
            
            // Note: We'd need to fetch user email from user-service or include it in the event
            // For now, assuming email is in the event or we have a way to resolve it
            String email = data.has("email") ? data.get("email").asText() : userId + "@example.com";
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", email.split("@")[0]);
            templateData.put("planName", planName);
            templateData.put("trialEndDate", trialEndDate);
            templateData.put("dashboardUrl", baseUrl + "/dashboard");
            templateData.put("features", java.util.List.of("Feature 1", "Feature 2", "Feature 3"));
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.SUBSCRIPTION_CREATED,
                email,
                "Welcome to " + planName + " Plan!",
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Subscription created email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process SubscriptionCreated event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process SubscriptionCreated event", e);
        }
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.payment-succeeded:payment.succeeded}")
    public void handlePaymentSucceeded(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received PaymentSucceeded event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode data = envelope.get("data");
            
            String userId = data.get("userId").asText();
            String amount = data.get("amount").asText();
            String planName = data.get("planName").asText();
            String billingPeriod = data.get("billingPeriod").asText();
            String paymentDate = data.get("paymentDate").asText();
            String invoiceId = data.has("invoiceId") ? data.get("invoiceId").asText() : null;
            
            String email = data.has("email") ? data.get("email").asText() : userId + "@example.com";
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", email.split("@")[0]);
            templateData.put("amount", amount);
            templateData.put("planName", planName);
            templateData.put("billingPeriod", billingPeriod);
            templateData.put("paymentDate", paymentDate);
            templateData.put("invoiceUrl", baseUrl + "/invoices/" + invoiceId);
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.PAYMENT_RECEIPT,
                email,
                "Payment Received - " + amount,
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Payment receipt email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process PaymentSucceeded event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PaymentSucceeded event", e);
        }
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.payment-failed:payment.failed}")
    public void handlePaymentFailed(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received PaymentFailed event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode data = envelope.get("data");
            
            String userId = data.get("userId").asText();
            String amount = data.get("amount").asText();
            String reason = data.has("reason") ? data.get("reason").asText() : "Payment processing failed";
            Integer gracePeriodDays = data.has("gracePeriodDays") ? data.get("gracePeriodDays").asInt() : 7;
            
            String email = data.has("email") ? data.get("email").asText() : userId + "@example.com";
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", email.split("@")[0]);
            templateData.put("amount", amount);
            templateData.put("reason", reason);
            templateData.put("gracePeriodDays", gracePeriodDays);
            templateData.put("updatePaymentUrl", baseUrl + "/settings/billing");
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.PAYMENT_FAILED,
                email,
                "Payment Failed - Action Required",
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Payment failed email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process PaymentFailed event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PaymentFailed event", e);
        }
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.subscription-canceled:payment.subscription.canceled}")
    public void handleSubscriptionCanceled(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received SubscriptionCanceled event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode data = envelope.get("data");
            
            String userId = data.get("userId").asText();
            String accessEndDate = data.has("accessEndDate") ? data.get("accessEndDate").asText() : null;
            
            String email = data.has("email") ? data.get("email").asText() : userId + "@example.com";
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", email.split("@")[0]);
            templateData.put("accessEndDate", accessEndDate);
            templateData.put("reactivateUrl", baseUrl + "/settings/billing");
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.SUBSCRIPTION_CANCELED,
                email,
                "Subscription Canceled",
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Subscription canceled email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process SubscriptionCanceled event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process SubscriptionCanceled event", e);
        }
    }
    
    @RetryableTopic(
        attempts = "4",
        dltTopicSuffix = ".dlq",
        autoCreateTopics = "true",
        topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "${kafka.topics.trial-expired:payment.trial.expired}")
    public void handleTrialExpired(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        
        log.info("Received TrialExpired event from topic: {}", topic);
        
        try {
            JsonNode envelope = objectMapper.readTree(message);
            JsonNode data = envelope.get("data");
            
            String userId = data.get("userId").asText();
            
            String email = data.has("email") ? data.get("email").asText() : userId + "@example.com";
            
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", email.split("@")[0]);
            templateData.put("pricingUrl", baseUrl + "/pricing");
            templateData.put("trialFeatures", java.util.List.of("Feature 1", "Feature 2", "Feature 3"));
            
            SendNotificationRequest request = new SendNotificationRequest(
                NotificationType.TRIAL_EXPIRED,
                email,
                "Your Free Trial Has Ended",
                templateData
            );
            
            sendNotificationUseCase.send(request);
            
            log.info("Trial expired email sent to {}", email);
            
        } catch (Exception e) {
            log.error("Failed to process TrialExpired event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process TrialExpired event", e);
        }
    }
    
    @DltHandler
    public void handleDlt(String message, @Header(KafkaHeaders.ORIGINAL_TOPIC) String originalTopic) {
        log.error("Message sent to DLT for topic {}: {}", originalTopic, message);
    }
}

