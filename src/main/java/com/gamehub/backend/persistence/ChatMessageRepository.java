package com.gamehub.backend.persistence;

import com.gamehub.backend.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderId(Long senderId);
    List<ChatMessage> findByReceiverId(Long receiverId);
    void deleteAllByTimestampBefore(LocalDateTime before);
}
