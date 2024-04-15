package com.Gamehub.backend.business.impl;

import com.Gamehub.backend.domain.Review;
import com.Gamehub.backend.domain.User;
import com.Gamehub.backend.domain.Game;
import com.Gamehub.backend.persistence.ReviewRepository;
import com.Gamehub.backend.persistence.UserRepository;
import com.Gamehub.backend.persistence.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private User user;
    private Game game;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("Test User");

        game = new Game();
        game.setId(1L);
        game.setTitle("Test Game");

        review = new Review();
        review.setId(1L);
        review.setUser(user);
        review.setGame(game);
        review.setRating(5);
        review.setComment("Excellent game with great mechanics.");
    }


    @Test
    void createReview() {
        Mockito.lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Mockito.lenient().when(gameRepository.findById(anyLong())).thenReturn(Optional.of(game));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review createdReview = reviewService.createReview(review, user.getId(), game.getId());

        assertNotNull(createdReview);
        assertEquals(review.getId(), createdReview.getId());
        verify(reviewRepository).save(any(Review.class));
    }
    @Test
    void createReviewWithNullReview() {
        Long userId = user.getId();
        Long gameId = game.getId();

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(null, userId, gameId);
        });
    }

    @Test
    void getReviewById() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        Optional<Review> foundReview = reviewService.getReviewsById(1L);

        assertTrue(foundReview.isPresent());
        assertEquals(review.getId(), foundReview.get().getId());
        verify(reviewRepository).findById(1L);
    }

    @Test
    void getReviewByNonexistentId() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());
        Optional<Review> result = reviewService.getReviewsById(999L);

        assertFalse(result.isPresent());
        verify(reviewRepository).findById(999L);
    }

    @Test
    void getAllReviews() {
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review));
        List<Review> reviews = reviewService.getAllReviews();

        assertFalse(reviews.isEmpty());
        assertEquals(1, reviews.size());
        verify(reviewRepository).findAll();
    }

    @Test
    void updateReview() {
        Review updatedReviewData = new Review();
        updatedReviewData.setRating(4);
        updatedReviewData.setComment("Great game, but a bit short.");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review updatedReview = reviewService.updateReview(1L, updatedReviewData);

        assertNotNull(updatedReview);
        assertEquals("Great game, but a bit short.", updatedReview.getComment());
        assertEquals(4, updatedReview.getRating());
        verify(reviewRepository).save(review);
    }

    @Test
    void updateNonexistentReview() {
        Review updatedReviewData = new Review();
        updatedReviewData.setRating(3);
        updatedReviewData.setComment("Moderate experience.");

        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            reviewService.updateReview(999L, updatedReviewData);
        });
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReview() {
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(1L);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteNonexistentReview() {
        doThrow(new RuntimeException("Review not found.")).when(reviewRepository).deleteById(999L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            reviewService.deleteReview(999L);
        });

        assertTrue(exception.getMessage().contains("Review not found"));
        verify(reviewRepository).deleteById(999L);
    }
}

