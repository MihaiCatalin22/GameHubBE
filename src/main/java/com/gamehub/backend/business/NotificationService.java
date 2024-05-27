package com.gamehub.backend.business;

import com.gamehub.backend.domain.Notification;
import com.gamehub.backend.dto.NotificationDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {
    Notification save(Notification notification);
    List<NotificationDTO> getUnreadNotifications(Long userId);
    void deleteOldNotifications(LocalDateTime before);
    void markAsRead(Long notificationId);
}
