package com.gamehub.backend.controller;

import com.gamehub.backend.business.NotificationService;
import com.gamehub.backend.domain.Notification;
import com.gamehub.backend.dto.NotificationDTO;
import com.gamehub.backend.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "http://localhost:5173", exposedHeaders = "Authorization")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationController(NotificationService notificationService, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/notifications/send")
    public NotificationDTO sendNotification(NotificationDTO notificationDTO) {
        return userRepository.findById(notificationDTO.getUserId())
                .map(user -> {
                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setMessage(notificationDTO.getMessage());
                    notification.setType(notificationDTO.getType());
                    notification.setSenderId(notificationDTO.getSenderId());
                    notification.setEventId(notificationDTO.getEventId());
                    Notification savedNotification = notificationService.save(notification);
                    NotificationDTO result = new NotificationDTO(
                            savedNotification.getId(),
                            user.getId(),
                            savedNotification.getMessage(),
                            savedNotification.getType(),
                            savedNotification.getSenderId(),
                            savedNotification.getEventId()
                    );
                    messagingTemplate.convertAndSend("/user/" + notificationDTO.getUserId() + "/queue/notifications", result);
                    return result;
                })
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping("/history")
    public List<NotificationDTO> getNotificationHistory(@RequestParam Long userId) {
        return notificationService.getUnreadNotifications(userId);
    }

    @PostMapping("/read/{id}")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}

