package com.gamehub.backend.persistence;

import com.gamehub.backend.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r JOIN FETCH r.game WHERE r.game.id = :gameId")
    List<Review> findByGameId(Long gameId);
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.game WHERE r.user.id = :userId")
    List<Review> findByUserId(Long userId);
}
