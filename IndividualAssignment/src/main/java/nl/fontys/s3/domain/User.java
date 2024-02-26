package nl.fontys.s3.domain;

import javax.management.relation.Role;
import java.util.HashSet;
import java.util.Set;
public class User {

    private Long id;
    private String username;
    private String email;
    private String password;

    private Set<Role> roles = new HashSet<>();
}
