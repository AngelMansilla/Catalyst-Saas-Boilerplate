package com.catalyst.notification.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for notification persistence.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Entity
@Table(name = "notification_log", schema = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;
    
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

