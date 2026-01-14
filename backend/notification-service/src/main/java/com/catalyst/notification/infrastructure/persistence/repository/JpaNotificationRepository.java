package com.catalyst.notification.infrastructure.persistence.repository;

import com.catalyst.notification.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA repository for notification entities.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Repository
public interface JpaNotificationRepository extends JpaRepository<NotificationEntity, UUID> {
}

