package com.Gamehub.backend.persistence;
import com.Gamehub.backend.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}