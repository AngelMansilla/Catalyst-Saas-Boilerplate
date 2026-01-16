package com.catalyst.notification.infrastructure.persistence.mapper;

import com.catalyst.notification.domain.model.Notification;
import com.catalyst.notification.domain.valueobject.NotificationStatus;
import com.catalyst.notification.domain.valueobject.NotificationType;
import com.catalyst.notification.infrastructure.persistence.entity.NotificationEntity;

/**
 * Mapper between Notification domain model and NotificationEntity.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public class NotificationMapper {
    
    public static NotificationEntity toEntity(Notification notification) {
        return NotificationEntity.builder()
            .id(notification.getId().value())
            .type(notification.getType().getCode())
            .recipientEmail(notification.getRecipient().toString())
            .subject(notification.getSubject())
            .status(notification.getStatus().name())
            .errorMessage(notification.getErrorMessage())
            .retryCount(notification.getRetryCount())
            .createdAt(notification.getCreatedAt())
            .sentAt(notification.getSentAt())
            .updatedAt(notification.getUpdatedAt())
            .build();
    }
    
    public static Notification toDomain(NotificationEntity entity) {
        // Note: This is a simplified mapping. In a real scenario, we'd need to
        // reconstruct the templateData from a separate table or JSON column.
        // For now, we'll use an empty map as template data is not persisted.
        
        Notification notification = Notification.create(
            NotificationType.fromCode(entity.getType()),
            new com.catalyst.notification.domain.valueobject.EmailAddress(entity.getRecipientEmail()),
            entity.getSubject(),
            null // Template data not persisted
        );
        
        // Use reflection to set the ID (since Notification doesn't expose a setter)
        try {
            java.lang.reflect.Field idField = Notification.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(notification, new com.catalyst.notification.domain.valueobject.NotificationId(entity.getId()));
            
            // Set status
            java.lang.reflect.Field statusField = Notification.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(notification, NotificationStatus.valueOf(entity.getStatus()));
            
            // Set other fields
            java.lang.reflect.Field errorMessageField = Notification.class.getDeclaredField("errorMessage");
            errorMessageField.setAccessible(true);
            errorMessageField.set(notification, entity.getErrorMessage());
            
            java.lang.reflect.Field retryCountField = Notification.class.getDeclaredField("retryCount");
            retryCountField.setAccessible(true);
            retryCountField.set(notification, entity.getRetryCount());
            
            java.lang.reflect.Field createdAtField = Notification.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(notification, entity.getCreatedAt());
            
            java.lang.reflect.Field sentAtField = Notification.class.getDeclaredField("sentAt");
            sentAtField.setAccessible(true);
            sentAtField.set(notification, entity.getSentAt());
            
            java.lang.reflect.Field updatedAtField = Notification.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(notification, entity.getUpdatedAt());
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to map NotificationEntity to Notification", e);
        }
        
        return notification;
    }
}

