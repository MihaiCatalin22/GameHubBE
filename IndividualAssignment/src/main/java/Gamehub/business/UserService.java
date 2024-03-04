package Gamehub.business;

import Gamehub.domain.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;


@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private long userIdSequence = 0L;

    @PostConstruct
    public void init() {
        User user1 = new User(++userIdSequence, "testUser1", "test@email.com", "password", new HashSet<>());
        users.put(user1.getId(), user1);

        User user2 = new User(++userIdSequence, "testUser2", "cool@email.com", "password", new HashSet<>());
        users.put(user2.getId(), user2);
    }
    public User createUser(User user) {
        user.setId(++userIdSequence);
        users.put(user.getId(), user);
        return user;
    }
    public Optional<User> getUserById(Long id) {
        return Optional.ofNullable(users.get(id));
    }
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    public User updateUser(Long id, User userDetails) {
        if (users.containsKey(id)) {
            userDetails.setId(id);
            users.put(id, userDetails);
            return userDetails;
        }
        else {
            throw new NoSuchElementException("User not found with id: " + id);
        }
    }
    public void deleteUser(Long id) {
        users.remove(id);
    }
}
