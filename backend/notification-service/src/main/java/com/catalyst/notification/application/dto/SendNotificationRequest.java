package com.catalyst.notification.application.dto;

import com.catalyst.notification.domain.valueobject.NotificationType;

import java.util.Map;

/**
 * DTO for sending a notification.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record SendNotificationRequest(
        NotificationType type,
        String recipientEmail,
        String subject,
        Map<String, Object> templateData
) {
}

