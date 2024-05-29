package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.Review;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.dto.ReviewDTO;
import com.gamehub.backend.persistence.ReviewRepository;
import com.gamehub.backend.persistence.UserRepository;
import com.gamehub.backend.persistence.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    private User user;
    private Game game;
    private Review review;
    private ReviewDTO reviewDTO;

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

        reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setRating(review.getRating());
        reviewDTO.setContent(review.getComment());
        reviewDTO.setCreationDate(new Date());
        reviewDTO.setGameId(game.getId());
        reviewDTO.setGameTitle(game.getTitle());
    }

    @Test
    void createReviewSuccess() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(gameRepository.findById(game.getId())).thenReturn(Optional.of(game));
        when(reviewRepository.save(any())).thenReturn(review);

        ReviewDTO createdReview = reviewService.createReview(reviewDTO, user.getId(), game.getId());

        assertNotNull(createdReview);
        assertEquals(reviewDTO.getId(), createdReview.getId());
        assertEquals(reviewDTO.getGameId(), createdReview.getGameId());
        verify(reviewRepository).save(any(Review.class));
    }



    @Test
    void getReviewByIdFound() {
        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        Optional<ReviewDTO> foundReview = reviewService.getReviewsById(review.getId());

        assertTrue(foundReview.isPresent());
        assertEquals(review.getId(), foundReview.get().getId());
        verify(reviewRepository).findById(review.getId());
    }

    @Test
    void getReviewByIdNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ReviewDTO> result = reviewService.getReviewsById(999L);

        assertFalse(result.isPresent());
        verify(reviewRepository).findById(999L);
    }

    @Test
    void getAllReviewsNotEmpty() {
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(review));

        List<ReviewDTO> reviews = reviewService.getAllReviews();

        assertFalse(reviews.isEmpty());
        assertEquals(1, reviews.size());
        verify(reviewRepository).findAll();
    }

    @Test
    void getReviewsByGameIdFound() {
        when(reviewRepository.findByGameId(game.getId())).thenReturn(Arrays.asList(review));

        List<ReviewDTO> reviews = reviewService.getReviewsByGameId(game.getId());

        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
        verify(reviewRepository).findByGameId(game.getId());
    }

    @Test
    void getReviewsByGameIdEmpty() {
        when(reviewRepository.findByGameId(999L)).thenReturn(Collections.emptyList());

        List<ReviewDTO> reviews = reviewService.getReviewsByGameId(999L);

        assertTrue(reviews.isEmpty());
        verify(reviewRepository).findByGameId(999L);
    }

    @Test
    void getReviewsByUserIdFound() {
        when(reviewRepository.findByUserId(user.getId())).thenReturn(Arrays.asList(review));

        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(user.getId());

        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
        verify(reviewRepository).findByUserId(user.getId());
    }

    @Test
    void getReviewsByUserIdEmpty() {
        when(reviewRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(999L);

        assertTrue(reviews.isEmpty());
        verify(reviewRepository).findByUserId(999L);
    }

    @Test
    void updateReviewFoundAndUpdated() {
        ReviewDTO updatedReviewDTO = new ReviewDTO();
        updatedReviewDTO.setRating(3);
        updatedReviewDTO.setContent("Updated comment");

        when(reviewRepository.findById(review.getId())).thenReturn(Optional.of(review));

        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewDTO updatedReview = reviewService.updateReview(review.getId(), updatedReviewDTO);

        assertNotNull(updatedReview);
        assertEquals(3, updatedReview.getRating());
        assertEquals("Updated comment", updatedReview.getContent());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void updateNonexistentReview() {
        ReviewDTO updatedReviewDTO = new ReviewDTO();
        updatedReviewDTO.setRating(3);
        updatedReviewDTO.setContent("Moderate experience.");

        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.updateReview(999L, updatedReviewDTO));

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    void deleteReviewExisting() {
        doNothing().when(reviewRepository).deleteById(review.getId());

        reviewService.deleteReview(review.getId());

        verify(reviewRepository).deleteById(review.getId());
    }

    @Test
    void deleteReviewNonExistent() {
        doThrow(new RuntimeException("Review not found")).when(reviewRepository).deleteById(999L);

        assertThrows(RuntimeException.class, () -> reviewService.deleteReview(999L));
        verify(reviewRepository).deleteById(999L);
    }
}

