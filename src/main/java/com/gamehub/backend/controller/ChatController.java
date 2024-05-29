package com.gamehub.backend.controller;

import com.gamehub.backend.business.ChatMessageService;
import com.gamehub.backend.business.NotificationService;
import com.gamehub.backend.domain.ChatMessage;
import com.gamehub.backend.domain.Notification;
import com.gamehub.backend.dto.ChatMessageDTO;
import com.gamehub.backend.dto.NotificationDTO;
import com.gamehub.backend.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "http://localhost:5173", exposedHeaders = "Authorization")
@Validated
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatController(ChatMessageService chatMessageService, UserRepository userRepository, NotificationService notificationService, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageService = chatMessageService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessageDTO messageDTO) {
        userRepository.findById(messageDTO.getSenderId())
                .flatMap(sender -> userRepository.findById(messageDTO.getReceiverId())
                        .map(receiver -> {
                            ChatMessage message = new ChatMessage();
                            message.setSender(sender);
                            message.setReceiver(receiver);
                            message.setContent(messageDTO.getContent());
                            message.setTimestamp(messageDTO.getTimestamp());
                            chatMessageService.save(message);

                            ChatMessageDTO responseDTO = new ChatMessageDTO(
                                    sender.getId(),
                                    receiver.getId(),
                                    sender.getUsername(),
                                    receiver.getUsername(),
                                    messageDTO.getContent(),
                                    message.getTimestamp()
                            );

                            messagingTemplate.convertAndSendToUser(
                                    messageDTO.getReceiverId().toString(), "/queue/messages", responseDTO
                            );

                            messagingTemplate.convertAndSendToUser(
                                    messageDTO.getSenderId().toString(), "/queue/messages", responseDTO
                            );

                            NotificationDTO notificationDTO = new NotificationDTO(
                                    null,
                                    receiver.getId(),
                                    "You have a new message from " + sender.getUsername(),
                                    "message",
                                    sender.getId(),
                                    null
                            );
                            messagingTemplate.convertAndSendToUser(
                                    receiver.getId().toString(), "/queue/notifications", notificationDTO
                            );
                            notificationService.save(new Notification(
                                    null,
                                    receiver,
                                    "You have a new message from " + sender.getUsername(),
                                    message.getTimestamp(),
                                    false,
                                    "message",
                                    sender.getId(),
                                    null
                            ));

                            return responseDTO;
                        }))
                .orElseThrow(() -> new IllegalArgumentException("Sender or Receiver not found"));
    }

    @GetMapping("/history")
    public List<ChatMessageDTO> getChatHistory(@RequestParam Long userId, @RequestParam Long friendId) {
        return chatMessageService.getChatMessagesBetweenUsers(userId, friendId).stream()
                .map(msg -> new ChatMessageDTO(
                        msg.getSender().getId(),
                        msg.getReceiver().getId(),
                        msg.getSender().getUsername(),
                        msg.getReceiver().getUsername(),
                        msg.getContent(),
                        msg.getTimestamp()
                ))
                .toList();
    }
}
