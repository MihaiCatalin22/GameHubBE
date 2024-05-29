package com.gamehub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    @NotNull(message = "Sender ID cannot be null")
    private Long senderId;
    @NotNull(message = "Receiver ID cannot be null")
    private Long receiverId;
    private String senderUsername;
    private String receiverUsername;
    @NotBlank(message = "Content cannot be empty")
    private String content;
    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp;
}
