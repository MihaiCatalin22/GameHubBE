package com.gamehub.backend.controller;

import com.gamehub.backend.business.impl.RecommendationServiceImpl;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.EnumSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationServiceImpl recommendationService;

    private Game sampleGame;

    @BeforeEach
    void setup() {
        sampleGame = new Game();
        sampleGame.setId(1L);
        sampleGame.setTitle("Cyberpunk 2077");
        sampleGame.setGenres(EnumSet.of(Genre.ACTION, Genre.OPEN_WORLD));

        given(recommendationService.getRecommendationsForUser(1L)).willReturn(Arrays.asList(sampleGame));
    }

    @Test
    @WithMockUser(username="user1", roles={"USER"})
    void getRecommendationsForUserTest() throws Exception {
        mockMvc.perform(get("/recommendations/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Cyberpunk 2077"));
    }
}
