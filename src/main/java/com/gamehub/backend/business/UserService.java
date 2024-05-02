package com.gamehub.backend.business;
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
}
