package Gamehub.domain;

<<<<<<< Updated upstream:src/main/java/Gamehub/domain/User.java
import javax.management.relation.Role;
import java.util.HashSet;
import java.util.Set;
=======
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

>>>>>>> Stashed changes:src/main/java/com/Gamehub/backend/domain/User.java
import lombok.*;
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
<<<<<<< Updated upstream:src/main/java/Gamehub/domain/User.java
=======

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "Role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private List<Role> roles = new ArrayList<>();

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String username, String email, String password, List<Role> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}
>>>>>>> Stashed changes:src/main/java/com/Gamehub/backend/domain/User.java

    private Set<Role> roles = new HashSet<>();
}
