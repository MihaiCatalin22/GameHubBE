package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Review;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.persistence.ReviewRepository;
import com.gamehub.backend.persistence.UserRepository;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.business.ReviewService;
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

