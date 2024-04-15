package com.Gamehub.backend.business.impl;

import com.Gamehub.backend.domain.Review;
import com.Gamehub.backend.domain.User;
import com.Gamehub.backend.domain.Game;
import com.Gamehub.backend.persistence.ReviewRepository;
import com.Gamehub.backend.persistence.UserRepository;
import com.Gamehub.backend.persistence.GameRepository;
import com.Gamehub.backend.business.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, UserRepository userRepository, GameRepository gameRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public Review createReview(Review review, Long userId, Long gameId) {
        if (review == null || review.getGame() == null || review.getUser() == null) {
            throw new IllegalArgumentException("Review, Game, and User cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found."));

        review.setUser(user);
        review.setGame(game);

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    @Override
    public Optional<Review> getReviewsById(Long id) {
        return reviewRepository.findById(id);
    }
    @Override
    public List<Review> getReviewsByGameId(Long gameId) {
        return reviewRepository.findByGameId(gameId);
    }

    @Override
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Override
    public Review updateReview(Long reviewId, Review review) {
        return reviewRepository.findById(reviewId)
                .map(existingReview -> {
                    existingReview.setRating(review.getRating());
                    existingReview.setComment(review.getComment());
                    return reviewRepository.save(existingReview);
                })
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}

