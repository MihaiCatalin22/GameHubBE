package com.gamehub.backend.business.impl;

import com.gamehub.backend.business.NotificationService;
import com.gamehub.backend.domain.Notification;
import com.gamehub.backend.dto.NotificationDTO;
import com.gamehub.backend.persistence.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
    @Override
    public Notification save(Notification notification) {
        notification.setTimestamp(LocalDateTime.now());
        return notificationRepository.save(notification);
    }

    @Override
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId).stream()
                .map(notification -> new NotificationDTO(
                        notification.getId(),
                        notification.getUser().getId(),
                        notification.getMessage(),
                        notification.getType(),
                        notification.getSenderId(),
                        notification.getEventId()
                ))
                .toList();
    }

    @Override
    public void deleteOldNotifications(LocalDateTime before) {
        notificationRepository.deleteAllByTimestampBefore(before);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
