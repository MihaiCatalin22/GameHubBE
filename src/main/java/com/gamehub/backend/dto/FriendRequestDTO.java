package com.gamehub.backend.dto;

import com.gamehub.backend.domain.FriendRelationship.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendRequestDTO {
    private Long id;
    @NotNull(message = "User cannot be null")
    private UserDTO user;
    @NotNull(message = "Friend cannot be null")
    private UserDTO friend;
    @NotNull(message = "Status cannot be null")
    private Status status;
}
