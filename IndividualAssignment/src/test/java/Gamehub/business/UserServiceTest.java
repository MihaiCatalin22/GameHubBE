package Gamehub.business;

import Gamehub.business.UserService;
import Gamehub.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        // Setup
        User newUser = new User(null, "testUser", "testuser@email.com", "password", new HashSet<>());

        // Run the test
        User createdUser = userServiceTest.createUser(newUser);

        // Verify the results
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getUsername()).isEqualTo("testUser");

    }

    @Test
    void getUserById() {
        // Setup
        User existingUser = userServiceTest.createUser(new User(null, "existingUser", "existing@example.com", "password", new HashSet<>()));

        // Run the test
        Optional<User> foundUser = userServiceTest.getUserById(existingUser.getId());

        // Verify the results
        assertTrue(foundUser.isPresent());
        assertThat(foundUser.get().getUsername()).isEqualTo("existingUser");
    }

    @Test
    void getAllUsers() {
        // Run the test
        List<User> users = userServiceTest.getAllUsers();

        // Verify the results
        assertThat(users).isNotEmpty();
    }

    @Test
    void updateUser() {
        // Setup
        User existingUser = userServiceTest.createUser(new User(null, "updateUser", "update@example.com", "password", new HashSet<>()));
        User updatedUser = new User(existingUser.getId(), "updatedUsername", "updated@example.com", "newPassword", new HashSet<>());

        // Run the test
        User result = userServiceTest.updateUser(existingUser.getId(), updatedUser);

        // Verify the results
        assertThat(result.getUsername()).isEqualTo("updatedUsername");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

    }

    @Test
    void deleteUser() {
        // Setup
        User userToDelete = userServiceTest.createUser(new User(null, "deleteUser", "delete@example.com", "password", new HashSet<>()));

        // Run the test
        userServiceTest.deleteUser(userToDelete.getId());

        // Verify the results
        Optional<User> deletedUser = userServiceTest.getUserById(userToDelete.getId());
        assertThat(deletedUser).isEmpty();
    }
}