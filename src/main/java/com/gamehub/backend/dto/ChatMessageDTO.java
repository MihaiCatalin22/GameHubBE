package com.gamehub.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private Long senderId;
    private Long receiverId;
    private String senderUsername;
    private String receiverUsername;
    private String content;
    private LocalDateTime timestamp;
}
