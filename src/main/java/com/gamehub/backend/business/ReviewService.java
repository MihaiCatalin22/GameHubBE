package com.gamehub.backend.business;

import com.gamehub.backend.dto.ReviewDTO;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    ReviewDTO createReview(ReviewDTO reviewDto, Long gameId, Long userId);
    List<ReviewDTO> getReviewsByGameId(Long gameId);
    List<ReviewDTO> getReviewsByUserId(Long userId);
    Optional<ReviewDTO> getReviewsById(Long id);
    List<ReviewDTO> getAllReviews();
    ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDto);
    void deleteReview(Long reviewId);
}
