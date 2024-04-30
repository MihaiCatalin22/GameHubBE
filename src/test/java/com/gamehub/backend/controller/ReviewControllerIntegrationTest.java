package com.gamehub.backend.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Review;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.business.ReviewService;
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

    private Review sampleReview;
    private User testUser;
    private Game testGame;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setTitle("Elden Ring");

        sampleReview = new Review();
        sampleReview.setId(1L);
        sampleReview.setUser(testUser);
        sampleReview.setGame(testGame);
        sampleReview.setRating(5);
        sampleReview.setComment("Great game!");
        sampleReview.setCreationDate(new Date());

        when(reviewService.updateReview(anyLong(), any(Review.class))).thenAnswer(invocation -> {
            Review r = invocation.getArgument(1);
            if (r.getUser() == null) {
                throw new IllegalArgumentException("User cannot be null");
            }
            return r;
        });
        given(reviewService.createReview(any(Review.class), anyLong(), anyLong())).willReturn(sampleReview);
        given(reviewService.getAllReviews()).willReturn(Arrays.asList(sampleReview));
        given(reviewService.getReviewsByGameId(anyLong())).willReturn(Arrays.asList(sampleReview));
        given(reviewService.getReviewsById(anyLong())).willReturn(Optional.of(sampleReview));
    }

    @Test
    @WithMockUser
    void addReviewToGameTest() throws Exception {
        mockMvc.perform(post("/reviews/games/{gameId}/review", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleReview))
                        .param("userId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(sampleReview.getRating()))
                .andExpect(jsonPath("$.comment").value(sampleReview.getComment()));
    }

    @Test
    void getAllReviewsTest() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setPasswordHash("encodedPassword");

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Review localSampleReview = new Review();
        localSampleReview.setId(1L);
        localSampleReview.setUser(testUser);
        localSampleReview.setGame(testGame);
        localSampleReview.setRating(5);
        localSampleReview.setComment("Great game!");
        localSampleReview.setCreationDate(new Date());

        given(reviewService.getAllReviews()).willReturn(Arrays.asList(localSampleReview));

        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(localSampleReview.getId()))
                .andExpect(jsonPath("$[0].rating").value(localSampleReview.getRating()));
    }

    @Test
    void getReviewsByGameIdTest() throws Exception {
        User user = new User();
        user.setUsername("user");
        user.setPasswordHash("encodedPassword");

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        sampleReview = new Review();
        sampleReview.setId(1L);
        sampleReview.setUser(testUser);
        sampleReview.setGame(testGame);
        sampleReview.setRating(5);
        sampleReview.setComment("Great game!");
        sampleReview.setCreationDate(new Date());


        mockMvc.perform(get("/reviews/games/{gameId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sampleReview.getId()))
                .andExpect(jsonPath("$[0].rating").value(5));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username="adminUser", roles={"ADMINISTRATOR"})
    void updateReviewTest() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("adminUser");
        user.setPasswordHash("encodedPassword");

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Review updatedReview = new Review();
        updatedReview.setId(1L);
        updatedReview.setUser(user);
        updatedReview.setGame(testGame);
        updatedReview.setRating(5);
        updatedReview.setComment("Updated comment");
        updatedReview.setCreationDate(new Date());

        given(reviewService.updateReview(eq(1L), any(Review.class))).willReturn(updatedReview);

        mockMvc.perform(put("/reviews/{reviewId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedReview)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("Updated comment"));

        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username="adminUser", roles={"ADMINISTRATOR"})
    void deleteReviewTest() throws Exception {
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPasswordHash("encodedPassword");

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR"));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(adminUser.getUsername(), adminUser.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        willDoNothing().given(reviewService).deleteReview(1L);

        mockMvc.perform(delete("/reviews/{reviewId}", 1L))
                .andExpect(status().isOk());

        SecurityContextHolder.clearContext();
    }
}
