package com.gamehub.backend.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Review;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.business.ReviewService;
import com.gamehub.backend.dto.AuthorInfo;
import com.gamehub.backend.dto.ReviewDTO;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.BDDMockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReviewDTO sampleReview;

    @BeforeEach
    void setup() {
        sampleReview = new ReviewDTO();
        sampleReview.setId(1L);
        sampleReview.setGameId(1L);
        AuthorInfo authorInfo = new AuthorInfo(1L, "user1");
        sampleReview.setAuthor(authorInfo);
        sampleReview.setRating(5);
        sampleReview.setContent("Great game!");

        given(reviewService.createReview(any(ReviewDTO.class), eq(1L), eq(1L))).willReturn(sampleReview);
        given(reviewService.getAllReviews()).willReturn(Arrays.asList(sampleReview));
        given(reviewService.getReviewsByGameId(anyLong())).willReturn(Arrays.asList(sampleReview));
        given(reviewService.getReviewsById(anyLong())).willReturn(Optional.of(sampleReview));
        given(reviewService.updateReview(anyLong(), any(ReviewDTO.class))).willAnswer(invocation -> invocation.getArgument(1));
    }
    private void setupSecurityContext(String username, String role, Long id) {
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername(username);
        adminUser.setPasswordHash("encodedPassword");

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        CustomUserDetails userDetails = new CustomUserDetails(id, username, "encodedPassword", authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        System.out.println("Security context set with username: " + userDetails.getUsername() + ", id: " + userDetails.getId() + ", authorities: " + userDetails.getAuthorities());
    }
    @Test
    @WithMockUser(username="adminUser", roles={"ADMINISTRATOR"})
    void addReviewToGameTest() throws Exception {
        setupSecurityContext("adminUser", "ADMINISTRATOR", 1L);

        mockMvc.perform(post("/reviews/games/{gameId}/review", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReview))
                        .param("userId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(sampleReview.getRating()))
                .andExpect(jsonPath("$.content").value(sampleReview.getContent()));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser
    void getAllReviewsTest() throws Exception {
        setupSecurityContext("testUser", "USER", 1L);

        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleReview.getId()))
                .andExpect(jsonPath("$[0].rating").value(sampleReview.getRating()));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser
    void getReviewsByGameIdTest() throws Exception {
        setupSecurityContext("testUser", "USER", 1L);

        mockMvc.perform(get("/reviews/games/{gameId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleReview.getId()))
                .andExpect(jsonPath("$[0].rating").value(5));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username="adminUser", roles={"ADMINISTRATOR"})
    void updateReviewTest() throws Exception {
        setupSecurityContext("adminUser", "ADMINISTRATOR", 1L);

        ReviewDTO updatedReview = new ReviewDTO();
        updatedReview.setId(1L);
        AuthorInfo authorInfo = new AuthorInfo(1L, "user1");
        updatedReview.setAuthor(authorInfo);
        updatedReview.setGameId(1L);
        updatedReview.setRating(4);
        updatedReview.setContent("Updated comment");

        given(reviewService.updateReview(eq(1L), any(ReviewDTO.class))).willReturn(updatedReview);

        System.out.println("Starting updateReviewTest");

        mockMvc.perform(put("/reviews/{reviewId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedReview)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"));

        System.out.println("Completed updateReviewTest");

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username="adminUser", roles={"ADMINISTRATOR"})
    void deleteReviewTest() throws Exception {
        setupSecurityContext("adminUser", "ADMINISTRATOR", 1L);

        willDoNothing().given(reviewService).deleteReview(1L);

        mockMvc.perform(delete("/reviews/{reviewId}", 1L))
                .andExpect(status().isOk());

        SecurityContextHolder.clearContext();
    }
}
