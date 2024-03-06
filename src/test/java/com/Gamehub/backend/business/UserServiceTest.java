package com.Gamehub.backend.business;

import com.Gamehub.backend.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserServiceTest {
    private UserService userServiceTest;

@BeforeEach
public void setup() {
    userServiceTest = new UserService();
    userServiceTest.init();
}

@Test
void createUser() {
    User newUser = new User(null, "testUser", "testuser@email.com", "password", new HashSet<>());

    User createdUser = userServiceTest.createUser(newUser);

    assertThat(createdUser).isNotNull();
    assertThat(createdUser.getId()).isNotNull();
    assertThat(createdUser.getUsername()).isEqualTo("testUser");

}

@Test
void getUserById() {
    User existingUser = userServiceTest.createUser(new User(null, "existingUser", "existing@email.com", "password", new HashSet<>()));

    Optional<User> foundUser = userServiceTest.getUserById(existingUser.getId());

    assertTrue(foundUser.isPresent());
    assertThat(foundUser.get().getUsername()).isEqualTo("existingUser");
}

@Test
void getAllUsers() {
    List<User> users = userServiceTest.getAllUsers();

    assertThat(users).isNotEmpty();
}

@Test
void updateUser() {
    User existingUser = userServiceTest.createUser(new User(null, "updateUser", "update@email.com", "password", new HashSet<>()));
    User updatedUser = new User(existingUser.getId(), "updatedUsername", "updated@email.com", "newPassword", new HashSet<>());

    User result = userServiceTest.updateUser(existingUser.getId(), updatedUser);

    assertThat(result.getUsername()).isEqualTo("updatedUsername");
    assertThat(result.getEmail()).isEqualTo("updated@email.com");

}

@Test
void deleteUser() {
    User userToDelete = userServiceTest.createUser(new User(null, "deleteUser", "delete@email.com", "password", new HashSet<>()));

    userServiceTest.deleteUser(userToDelete.getId());

    Optional<User> deletedUser = userServiceTest.getUserById(userToDelete.getId());
    assertThat(deletedUser).isEmpty();
}

@Test
void getUserById_whenUserDoesNotExist() {
    Long nonExistentId = 999L;
    Optional<User> foundUser = userServiceTest.getUserById(nonExistentId);
    assertTrue(foundUser.isEmpty(), "Expected no user to be found with a non-existent ID");
}

@Test
void updateUser_whenUserDoesNotExist() {
    Long nonExistentId = 999L;
    User updateUser = new User(nonExistentId, "nonExistentUser", "nonexistent@email.com", "password", new HashSet<>());

    Exception exception = assertThrows(NoSuchElementException.class, () -> {
        userServiceTest.updateUser(nonExistentId, updateUser);
    });

    String expectedMessage = "User not found with id: " + nonExistentId;
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
}

@Test
void deleteUser_whenUserDoesNotExist() {
    Long nonExistentId = 999L;
    assertDoesNotThrow(() -> userServiceTest.deleteUser(nonExistentId),
            "Deleting a non-existent user should not throw an exception");
}

@Test
void createUser_withInvalidData() {
    User invalidUser = new User(null, "", "", "password", new HashSet<>());
}


}