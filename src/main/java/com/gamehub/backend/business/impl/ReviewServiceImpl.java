package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Review;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.dto.ReviewDTO;
import com.gamehub.backend.persistence.ReviewRepository;
import com.gamehub.backend.persistence.UserRepository;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.business.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setComment(review.getComment());
        dto.setCreationDate(review.getCreationDate());
        if (review.getGame() != null) {
            dto.setGameTitle(review.getGame().getTitle());
            dto.setGameId(review.getGame().getId());
        }
        return dto;
    }

    private Review convertToEntity(ReviewDTO reviewDto, Long gameId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new IllegalArgumentException("Game not found."));

        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setContent(reviewDto.getContent());
        review.setComment(reviewDto.getComment());
        review.setUser(user);
        review.setGame(game);
        return review;
    }

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDto, Long gameId, Long userId) {
        validateReview(reviewDto);
        Review review = convertToEntity(reviewDto, gameId, userId);
        review = reviewRepository.save(review);
        return convertToDTO(review);
    }

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    @Override
    public Optional<ReviewDTO> getReviewsById(Long id) {
        return reviewRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public List<ReviewDTO> getReviewsByGameId(Long gameId) {
        return reviewRepository.findByGameId(gameId).stream().map(this::convertToDTO).toList();
    }

    @Override
    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(review -> {
                    ReviewDTO dto = new ReviewDTO();
                    dto.setId(review.getId());
                    dto.setContent(review.getContent());
                    dto.setComment(review.getComment());
                    dto.setRating(review.getRating());
                    dto.setGameId(review.getGame() != null ? review.getGame().getId() : null);
                    dto.setGameTitle(review.getGame() != null ? review.getGame().getTitle() : "No game associated");
                    return dto;
                }).toList();
    }

    @Override
    public ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDto) {
        validateReview(reviewDto);
        return reviewRepository.findById(reviewId)
                .map(existingReview -> {
                    existingReview.setRating(reviewDto.getRating());
                    existingReview.setComment(reviewDto.getComment());
                    existingReview.setContent(reviewDto.getContent());
                    reviewRepository.save(existingReview);
                    return convertToDTO(existingReview);
                })
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    private void validateReview(ReviewDTO reviewDto) {
        if (reviewDto.getRating() == null || reviewDto.getRating() < 1 || reviewDto.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
