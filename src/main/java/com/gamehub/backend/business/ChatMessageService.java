package com.gamehub.backend.business;

import com.gamehub.backend.domain.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageService {
    ChatMessage save(ChatMessage chatMessage);
    List<ChatMessage> getChatMessages(Long userId);
    List<ChatMessage> getChatMessagesBetweenUsers(Long userId, Long friendId);
    void deleteOldMessages(LocalDateTime before);
}
