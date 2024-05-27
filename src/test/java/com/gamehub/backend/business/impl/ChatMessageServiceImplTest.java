package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.ChatMessage;
import com.gamehub.backend.domain.Role;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatMessageServiceImpl chatMessageService;

    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setSender(new User(1L, "sender", "sender@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null));
        chatMessage.setReceiver(new User(2L, "receiver", "receiver@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null));
        chatMessage.setContent("Hello!");
        chatMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void saveChatMessage() {
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);

        ChatMessage savedMessage = chatMessageService.save(chatMessage);

        assertNotNull(savedMessage);
        assertEquals(chatMessage.getId(), savedMessage.getId());
        verify(chatMessageRepository).save(any(ChatMessage.class));
    }

    @Test
    void getChatMessages() {
        when(chatMessageRepository.findBySenderId(1L)).thenReturn(List.of(chatMessage));
        when(chatMessageRepository.findByReceiverId(1L)).thenReturn(List.of());

        List<ChatMessage> messages = chatMessageService.getChatMessages(1L);

        assertNotNull(messages);
        assertEquals(1, messages.size());
        assertEquals(chatMessage.getId(), messages.get(0).getId());
        verify(chatMessageRepository).findBySenderId(1L);
        verify(chatMessageRepository).findByReceiverId(1L);
    }

    @Test
    void getChatMessagesBetweenUsers() {
        ChatMessage receivedMessage = new ChatMessage();
        receivedMessage.setId(2L);
        receivedMessage.setSender(new User(2L, "receiver", "receiver@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null));
        receivedMessage.setReceiver(new User(1L, "sender", "sender@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null));
        receivedMessage.setContent("Hi there!");
        receivedMessage.setTimestamp(LocalDateTime.now());

        when(chatMessageRepository.findBySenderId(1L)).thenReturn(List.of(chatMessage));
        when(chatMessageRepository.findByReceiverId(1L)).thenReturn(List.of(receivedMessage));

        List<ChatMessage> messages = chatMessageService.getChatMessagesBetweenUsers(1L, 2L);

        assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals(chatMessage.getId(), messages.get(0).getId());
        assertEquals(receivedMessage.getId(), messages.get(1).getId());
        verify(chatMessageRepository).findBySenderId(1L);
        verify(chatMessageRepository).findByReceiverId(1L);
    }

    @Test
    void deleteOldMessages() {
        LocalDateTime timestamp = LocalDateTime.now().minusWeeks(1);

        doNothing().when(chatMessageRepository).deleteAllByTimestampBefore(timestamp);

        chatMessageService.deleteOldMessages(timestamp);

        verify(chatMessageRepository).deleteAllByTimestampBefore(timestamp);
    }
}