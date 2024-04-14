package com.Gamehub.backend.business;

import com.Gamehub.backend.domain.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review createReview(Review review);
    List<Review> getReviewsByGameId(Long gameId);
    List<Review> getReviewsByUserId(Long userId);
    Optional<Review> getReviewsById(Long id);
    List<Review> getAllReviews();
    Review updateReview(Long reviewId, Review reviewDetails);
    void deleteReview(Long reviewId);
}
