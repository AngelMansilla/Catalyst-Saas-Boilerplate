package com.catalyst.notification.application.ports.input;

import com.catalyst.notification.application.dto.SendNotificationRequest;
import com.catalyst.notification.application.dto.NotificationResponse;

/**
 * Use case for sending email notifications.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface SendNotificationUseCase {
    
    /**
     * Sends an email notification.
     * 
     * @param request the notification data
     * @return the notification response with status
     * @throws com.catalyst.notification.domain.exception.EmailDeliveryException if delivery fails
     * @throws com.catalyst.notification.domain.exception.TemplateNotFoundException if template not found
     */
    NotificationResponse send(SendNotificationRequest request);
}

