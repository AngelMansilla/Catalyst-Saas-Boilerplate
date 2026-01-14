package com.catalyst.notification.infrastructure.persistence.adapter;

import com.catalyst.notification.application.ports.output.NotificationRepository;
import com.catalyst.notification.domain.model.Notification;
import com.catalyst.notification.domain.valueobject.NotificationId;
import com.catalyst.notification.infrastructure.persistence.entity.NotificationEntity;
import com.catalyst.notification.infrastructure.persistence.mapper.NotificationMapper;
import com.catalyst.notification.infrastructure.persistence.repository.JpaNotificationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA adapter for NotificationRepository port.
 * 
 * @author Catalyst Team
 * @since 0.1.0
 */
@Component
public class NotificationRepositoryAdapter implements NotificationRepository {
    
    private final JpaNotificationRepository jpaRepository;
    
    public NotificationRepositoryAdapter(JpaNotificationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = NotificationMapper.toEntity(notification);
        NotificationEntity saved = jpaRepository.save(entity);
        return NotificationMapper.toDomain(saved);
    }
    
    @Override
    public Optional<Notification> findById(NotificationId id) {
        return jpaRepository.findById(id.value())
            .map(NotificationMapper::toDomain);
    }
}

