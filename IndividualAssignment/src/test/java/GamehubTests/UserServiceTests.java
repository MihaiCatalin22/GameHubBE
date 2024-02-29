package GamehubTests;

import Gamehub.business.UserService;
import Gamehub.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTests {
    private UserService userService;
    private User user;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("testPassword");
        userService.createUser(user);
    }
    @Test
    public void whenCreateUser_thenUserShouldBeFound() {
        User found = userService.getUserById(user.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo(user.getUsername());
    }
    @Test
    public void whenUpdateUser_thenUserDataShouldBeUpdated() {
        User updatedUser = new User();
        updatedUser.setUsername("updatedUser");
        updatedUser.setEmail("update@example.com");
        updatedUser.setPassword("newPassword");
        userService.updateUser(user.getId(), updatedUser);

        User found = userService.getUserById(user.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo(updatedUser.getUsername());
        assertThat(found.getEmail()).isEqualTo(updatedUser.getEmail());
        assertThat(found.getPassword()).isEqualTo(updatedUser.getPassword());
    }

    @Test
    public void whenDeleteUser_thenUserShouldNotBeFound() {
        userService.deleteUser(user.getId());
        User found = userService.getUserById(user.getId()).orElse(null);
        assertThat(found).isNull();
    }

    @Test
    public void whenGetAllUsers_thenReturnUserList() {
        List<User> users = userService.getAllUsers();
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThan(0);
        assertThat(users.contains(user)).isTrue();
    }
}