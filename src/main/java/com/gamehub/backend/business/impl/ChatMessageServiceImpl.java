package com.gamehub.backend.business.impl;

import com.gamehub.backend.business.ChatMessageService;
import com.gamehub.backend.domain.ChatMessage;
import com.gamehub.backend.persistence.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatMessageServiceImpl(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        chatMessage.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> getChatMessages(Long userId) {
        List<ChatMessage> sentMessages = chatMessageRepository.findBySenderId(userId);
        List<ChatMessage> receivedMessages = chatMessageRepository.findByReceiverId(userId);
        List<ChatMessage> allMessages = new ArrayList<>(sentMessages);
        allMessages.addAll(receivedMessages);
        return allMessages;
    }

    @Override
    public List<ChatMessage> getChatMessagesBetweenUsers(Long userId, Long friendId) {
        List<ChatMessage> sentMessages = chatMessageRepository.findBySenderId(userId);
        List<ChatMessage> receivedMessages = chatMessageRepository.findByReceiverId(userId);

        List<ChatMessage> allMessages = new ArrayList<>();

        for (ChatMessage message : sentMessages) {
            if (message.getReceiver().getId().equals(friendId)) {
                allMessages.add(message);
            }
        }

        for (ChatMessage message : receivedMessages) {
            if (message.getSender().getId().equals(friendId)) {
                allMessages.add(message);
            }
        }

        allMessages.sort(Comparator.comparing(ChatMessage::getTimestamp));

        return allMessages;
    }

    @Override
    public void deleteOldMessages(LocalDateTime before) {
        chatMessageRepository.deleteAllByTimestampBefore(before);
    }
}
