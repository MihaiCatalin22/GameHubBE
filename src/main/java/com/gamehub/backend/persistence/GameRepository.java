package com.gamehub.backend.persistence;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("SELECT g FROM Game g WHERE :genres MEMBER OF g.genres AND g.id NOT IN :ownedGameIds")
    List<Game> findRecommendations(@Param("genres")Genre genres, @Param("ownedGameIds") Set<Long> ownedGameIds);
    boolean existsByTitleAndIdNot(String title, Long id);
}