package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.ChatMessage;
import com.gamehub.backend.domain.FriendRelationship;
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

    private User sender;
    private User receiver;
    private ChatMessage chatMessage;
    private ChatMessage receivedMessage;
    private ChatMessage sentMessageToFriend;
    private ChatMessage receivedMessageFromFriend;

    @BeforeEach
    void setUp() {
        sender = new User(1L, "sender", "sender@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null, null, null);
        receiver = new User(2L, "receiver", "receiver@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null, null, null);

        FriendRelationship friendRelationship = new FriendRelationship();
        friendRelationship.setId(1L);
        friendRelationship.setStatus(FriendRelationship.Status.ACCEPTED);
        friendRelationship.setUser(sender);
        friendRelationship.setFriend(receiver);

        sender.setFriendRelationships(List.of(friendRelationship));
        receiver.setFriendRelationships(List.of(friendRelationship));

        chatMessage = new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setContent("Hello!");
        chatMessage.setTimestamp(LocalDateTime.now());

        receivedMessage = new ChatMessage();
        receivedMessage.setId(2L);
        receivedMessage.setSender(receiver);
        receivedMessage.setReceiver(sender);
        receivedMessage.setContent("Hi there!");
        receivedMessage.setTimestamp(LocalDateTime.now());

        sentMessageToFriend = new ChatMessage();
        sentMessageToFriend.setId(3L);
        sentMessageToFriend.setSender(sender);
        sentMessageToFriend.setReceiver(receiver);
        sentMessageToFriend.setContent("Message to friend");
        sentMessageToFriend.setTimestamp(LocalDateTime.now());

        receivedMessageFromFriend = new ChatMessage();
        receivedMessageFromFriend.setId(4L);
        receivedMessageFromFriend.setSender(receiver);
        receivedMessageFromFriend.setReceiver(sender);
        receivedMessageFromFriend.setContent("Message from friend");
        receivedMessageFromFriend.setTimestamp(LocalDateTime.now());
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
        when(chatMessageRepository.findBySenderId(1L)).thenReturn(List.of(chatMessage, sentMessageToFriend));
        when(chatMessageRepository.findByReceiverId(1L)).thenReturn(List.of(receivedMessage, receivedMessageFromFriend));

        List<ChatMessage> messages = chatMessageService.getChatMessagesBetweenUsers(1L, 2L);

        assertNotNull(messages);
        assertEquals(4, messages.size());
        assertEquals(chatMessage.getId(), messages.get(0).getId());
        assertEquals(sentMessageToFriend.getId(), messages.get(1).getId());
        assertEquals(receivedMessage.getId(), messages.get(2).getId());
        assertEquals(receivedMessageFromFriend.getId(), messages.get(3).getId());
        verify(chatMessageRepository).findBySenderId(1L);
        verify(chatMessageRepository).findByReceiverId(1L);
    }

    @Test
    void getChatMessagesBetweenUsers_withMixedConditions() {
        ChatMessage sentMessageToOtherUser = new ChatMessage();
        sentMessageToOtherUser.setId(5L);
        sentMessageToOtherUser.setSender(sender);
        sentMessageToOtherUser.setReceiver(new User(3L, "otherUser", "otherUser@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null, null, null));
        sentMessageToOtherUser.setContent("Message to other user");
        sentMessageToOtherUser.setTimestamp(LocalDateTime.now());

        ChatMessage receivedMessageFromOtherUser = new ChatMessage();
        receivedMessageFromOtherUser.setId(6L);
        receivedMessageFromOtherUser.setSender(new User(3L, "otherUser", "otherUser@example.com", "hashedPassword", "profilePic", "description", List.of(Role.USER), null, null, null, null, null));
        receivedMessageFromOtherUser.setReceiver(sender);
        receivedMessageFromOtherUser.setContent("Message from other user");
        receivedMessageFromOtherUser.setTimestamp(LocalDateTime.now());

        when(chatMessageRepository.findBySenderId(1L)).thenReturn(List.of(chatMessage, sentMessageToFriend, sentMessageToOtherUser));
        when(chatMessageRepository.findByReceiverId(1L)).thenReturn(List.of(receivedMessage, receivedMessageFromFriend, receivedMessageFromOtherUser));

        List<ChatMessage> messages = chatMessageService.getChatMessagesBetweenUsers(1L, 2L);

        assertNotNull(messages);
        assertEquals(4, messages.size());
        assertEquals(chatMessage.getId(), messages.get(0).getId());
        assertEquals(sentMessageToFriend.getId(), messages.get(1).getId());
        assertEquals(receivedMessage.getId(), messages.get(2).getId());
        assertEquals(receivedMessageFromFriend.getId(), messages.get(3).getId());
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
