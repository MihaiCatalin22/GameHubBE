package com.Gamehub.backend.business;
import com.Gamehub.backend.domain.User;
public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User user);
    void deleteUser(Long id);
}
