package com.gamehub.backend.persistence;

import com.gamehub.backend.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    void deleteAllByTimestampBefore(LocalDateTime before);
}
