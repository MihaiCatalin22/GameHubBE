package com.gamehub.backend.persistence;
import com.gamehub.backend.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}