package com.gamehub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    @NotBlank(message = "Message cannot be empty")
    private String message;
    @NotBlank(message = "Type cannot be empty")
    private String type;
    private Long senderId;
    private Long eventId;
}
