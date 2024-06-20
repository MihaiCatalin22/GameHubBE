package com.gamehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Genre;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GameControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Game sampleGame;

    @BeforeEach
    void setup() {
        sampleGame = new Game();
        sampleGame.setId(1L);
        sampleGame.setTitle("Elden Ring");
        sampleGame.setDescription("A challenging action RPG.");
        sampleGame.setGenres(new HashSet<>(Arrays.asList(Genre.ACTION, Genre.RPG)));
        sampleGame.setReleaseDate(new Date());
        sampleGame.setDeveloper("FromSoftware");
        sampleGame.setPrice(19.99);
        given(gameRepository.findById(1L)).willReturn(Optional.of(sampleGame));
        given(gameRepository.save(any(Game.class))).willReturn(sampleGame);
        given(gameRepository.findAll()).willReturn(Arrays.asList(sampleGame));
    }

    private void setupAuthentication(String username, String role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@mail.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        CustomUserDetails userDetails = new CustomUserDetails(user.getId(), user.getUsername(), user.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMINISTRATOR"})
    void createGameSuccessTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleGame)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Elden Ring"))
                .andExpect(jsonPath("$.price").value(19.99));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMINISTRATOR"})
    void createGameValidationErrorTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        Game invalidGame = new Game();
        invalidGame.setTitle("");

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidGame)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGameByIdSuccessTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        mockMvc.perform(get("/games/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Elden Ring"))
                .andExpect(jsonPath("$.developer").value("FromSoftware"));
    }

    @Test
    void getGameByIdNotFoundTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        mockMvc.perform(get("/games/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllGamesSuccessTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Elden Ring"))
                .andExpect(jsonPath("$[0].developer").value("FromSoftware"));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMINISTRATOR"})
    void updateGameSuccessTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        Game updatedGame = new Game();
        updatedGame.setTitle("Elden Ring Updated");
        updatedGame.setPrice(49.99);
        updatedGame.setReleaseDate(new Date());
        updatedGame.setDeveloper("FromSoftware");

        mockMvc.perform(put("/games/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGame)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Elden Ring Updated"))
                .andExpect(jsonPath("$.price").value(49.99));
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMINISTRATOR"})
    void updateGameNotFoundTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        Game updatedGame = new Game();
        updatedGame.setTitle("Elden Ring Updated");
        updatedGame.setPrice(49.99);
        updatedGame.setDescription("Updated description");
        updatedGame.setDeveloper("FromSoftware");
        updatedGame.setReleaseDate(new Date());

        mockMvc.perform(put("/games/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGame)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMINISTRATOR"})
    void deleteGameSuccessTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        mockMvc.perform(delete("/games/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username="admin", roles={"ADMINISTRATOR"})
    void deleteGameNotFoundTest() throws Exception {
        setupAuthentication("admin", "ADMINISTRATOR");

        mockMvc.perform(delete("/games/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
