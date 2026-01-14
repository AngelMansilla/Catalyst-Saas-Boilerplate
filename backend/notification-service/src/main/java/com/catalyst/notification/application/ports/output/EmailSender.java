package com.catalyst.notification.application.ports.output;

import com.catalyst.notification.domain.model.Notification;

/**
 * Port for sending email notifications.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
public interface EmailSender {
    
    /**
     * Sends an email notification.
     * 
     * @param notification the notification to send
     * @throws com.catalyst.notification.domain.exception.EmailDeliveryException if delivery fails
     */
    void send(Notification notification);
}

