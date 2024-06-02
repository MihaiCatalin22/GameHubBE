package com.gamehub.backend.business;
import com.gamehub.backend.domain.FriendRelationship;
import com.gamehub.backend.dto.FriendRequestDTO;
import com.gamehub.backend.dto.UserDTO;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO createUser(UserDTO userDTO);
    Optional<UserDTO> getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO userDTO);
    void updateUserProfilePicture(Long id, String fileName);
    void deleteUser(Long id);
    Optional<UserDTO> login(UserDTO userDTO);
    FriendRelationship sendRequest(Long userId, Long friendId);
    List<FriendRequestDTO> getPendingRequests(Long userId);
    FriendRelationship respondToRequest(Long relationshipId, FriendRelationship.Status status);
    List<FriendRequestDTO> getFriends(Long userId);
    void removeFriend(Long relationshipId);

    boolean verifyUsername(String username);
    boolean resetPassword(String username, String newPassword);
}
