package com.gamehub.backend.configuration;

import com.gamehub.backend.business.ChatMessageService;
import com.gamehub.backend.business.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledCleanupTask {

    private final ChatMessageService chatMessageService;
    private final NotificationService notificationService;

    @Autowired
    public ScheduledCleanupTask(ChatMessageService chatMessageService, NotificationService notificationService) {
        this.chatMessageService = chatMessageService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldMessages() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        chatMessageService.deleteOldMessages(oneWeekAgo);
        notificationService.deleteOldNotifications(oneWeekAgo);
    }
}
