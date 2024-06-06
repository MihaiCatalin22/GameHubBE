package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Notification;
import com.gamehub.backend.domain.Role;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.NotificationDTO;
import com.gamehub.backend.persistence.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(1L);
        notification.setUser(new User(1L, "user", "user@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null, null, null));
        notification.setMessage("New friend request");
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notification.setType("friend_request");
        notification.setSenderId(2L);
        notification.setEventId(null);
    }

    @Test
    void saveNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification savedNotification = notificationService.save(notification);

        assertNotNull(savedNotification);
        assertEquals(notification.getId(), savedNotification.getId());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void saveNotification_nullNotification() {
        assertThrows(NullPointerException.class, () -> {
            notificationService.save(null);
        });
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getUnreadNotifications() {
        when(notificationRepository.findByUserIdAndIsReadFalse(1L)).thenReturn(List.of(notification));

        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(1L);

        assertNotNull(notifications);
        assertEquals(1, notifications.size());
        assertEquals(notification.getId(), notifications.get(0).getId());
        verify(notificationRepository).findByUserIdAndIsReadFalse(1L);
    }

    @Test
    void getUnreadNotifications_noNotifications() {
        when(notificationRepository.findByUserIdAndIsReadFalse(1L)).thenReturn(Collections.emptyList());

        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(1L);

        assertNotNull(notifications);
        assertTrue(notifications.isEmpty());
        verify(notificationRepository).findByUserIdAndIsReadFalse(1L);
    }

    @Test
    void deleteOldNotifications() {
        LocalDateTime timestamp = LocalDateTime.now().minusWeeks(1);

        doNothing().when(notificationRepository).deleteAllByTimestampBefore(timestamp);

        notificationService.deleteOldNotifications(timestamp);

        verify(notificationRepository).deleteAllByTimestampBefore(timestamp);
    }

    @Test
    void markAsRead() {
        when(notificationRepository.findById(1L)).thenReturn(java.util.Optional.of(notification));

        notificationService.markAsRead(1L);

        assertEquals(true, notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_nonExistentNotification() {
        when(notificationRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        notificationService.markAsRead(1L);

        verify(notificationRepository, never()).save(any(Notification.class));
    }
}