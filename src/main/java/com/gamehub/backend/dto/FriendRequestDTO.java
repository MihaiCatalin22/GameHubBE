package com.gamehub.backend.dto;

import com.gamehub.backend.domain.FriendRelationship.Status;
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
    private UserDTO user;
    private UserDTO friend;
    private Status status;
}
