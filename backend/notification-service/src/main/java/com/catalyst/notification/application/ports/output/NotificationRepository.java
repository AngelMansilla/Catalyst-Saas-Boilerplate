package com.catalyst.notification.application.ports.output;

import com.catalyst.notification.domain.model.Notification;
import com.catalyst.notification.domain.valueobject.NotificationId;

import java.util.Optional;

/**
 * Port for persisting notifications.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface NotificationRepository {
    
    /**
     * Saves a notification.
     * 
     * @param notification the notification to save
     * @return the saved notification
     */
    Notification save(Notification notification);
    
    /**
     * Finds a notification by ID.
     * 
     * @param id the notification ID
     * @return the notification if found
     */
    Optional<Notification> findById(NotificationId id);
}

