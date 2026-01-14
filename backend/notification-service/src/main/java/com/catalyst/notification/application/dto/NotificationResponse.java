package com.catalyst.notification.application.dto;

import com.catalyst.notification.domain.model.Notification;
import com.catalyst.notification.domain.valueobject.NotificationStatus;

import java.time.LocalDateTime;

/**
 * DTO for notification response.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public record NotificationResponse(
        String id,
        String type,
        String recipientEmail,
        String subject,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime sentAt
) {
    
    public static NotificationResponse fromDomain(Notification notification) {
        return new NotificationResponse(
            notification.getId().toString(),
            notification.getType().getCode(),
            notification.getRecipient().toString(),
            notification.getSubject(),
            notification.getStatus(),
            notification.getCreatedAt(),
            notification.getSentAt()
        );
    }
}

