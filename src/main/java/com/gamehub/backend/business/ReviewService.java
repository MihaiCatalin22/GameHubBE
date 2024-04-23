package com.gamehub.backend.business;

import com.gamehub.backend.domain.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review createReview(Review review, Long gameId, Long userId);
    List<Review> getReviewsByGameId(Long gameId);
    List<Review> getReviewsByUserId(Long userId);
    Optional<Review> getReviewsById(Long id);
    List<Review> getAllReviews();
    Review updateReview(Long reviewId, Review reviewDetails);
    void deleteReview(Long reviewId);
}
