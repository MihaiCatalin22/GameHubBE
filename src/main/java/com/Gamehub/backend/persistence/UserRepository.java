package com.Gamehub.backend.persistence;

import com.Gamehub.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
public interface UserRepository extends JpaRepository<User, Long> {
}
