package com.catalyst.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the Catalyst Notification Service.
 * 
 * <p>This service handles:
 * <ul>
 *   <li>Kafka event consumption (user-service, payment-service)</li>
 *   <li>Email notification processing</li>
 *   <li>Thymeleaf template rendering</li>
 *   <li>SMTP delivery (Mailpit dev, AWS SES prod)</li>
 *   <li>Notification logging and audit trail</li>
 * </ul>
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.catalyst.notification",
    "com.catalyst.shared"
})
@ConfigurationPropertiesScan
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

