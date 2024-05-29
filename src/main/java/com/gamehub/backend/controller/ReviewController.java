package com.gamehub.backend.controller;

import com.gamehub.backend.business.ReviewService;
import com.gamehub.backend.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/reviews")
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/games/{gameId}/review")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDTO> addReviewToGame(@PathVariable Long gameId, @Valid @RequestBody ReviewDTO reviewDto, @RequestParam Long userId) {
        try {
            ReviewDTO createdReview = reviewService.createReview(reviewDto, gameId, userId);
            return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            ReviewDTO errorReview = new ReviewDTO();
            errorReview.setContent("Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorReview);
        }
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewsById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByGameId(@PathVariable Long gameId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByGameId(gameId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUserId(@PathVariable Long userId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
        if (reviews.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("authentication.principal.id == #reviewDto.author.id or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<ReviewDTO> updateReview(@PathVariable Long reviewId, @Valid @RequestBody ReviewDTO reviewDto) {
        ReviewDTO updatedReview = reviewService.updateReview(reviewId, reviewDto);
        return ResponseEntity.ok(updatedReview);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok().build();
    }
}
