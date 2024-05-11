package com.gamehub.backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        User user = new User();
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPasswordHash("password");
        user = userRepository.save(user);

        CustomUserDetails customUserDetails = new CustomUserDetails(
                user.getId(), user.getUsername(), user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR")));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createUserTest() throws Exception {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", "newUser");
        newUser.put("email", "newuser@example.com");
        newUser.put("password", "securePassword123");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"));
    }

    @Test
    void getAllUsersTest() throws Exception {
        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getUserByIdTest() throws Exception {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setEmail("test@example.com");
        newUser.setPasswordHash("password");
        userRepository.save(newUser);

        mockMvc.perform(get("/users/" + newUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newUser.getId()));
    }

    @Test
    void updateUserTest() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("originalUser");
        existingUser.setEmail("original@example.com");
        existingUser.setDescription("Original description");
        existingUser.setPasswordHash("testPassword");

        existingUser = userRepository.save(existingUser);

        CustomUserDetails customUserDetails = new CustomUserDetails(
                existingUser.getId(), existingUser.getUsername(), existingUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR")));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        existingUser.setUsername("updatedUser");
        existingUser.setEmail("updated@example.com");
        existingUser.setDescription("Updated description");
        existingUser.setPasswordHash("updatedPassword");

        mockMvc.perform(put("/users/" + existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void loginUserTest() throws Exception {
        String rawPassword = "password123";
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(newUser);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "newUser");
        loginRequest.put("password", rawPassword);

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"));
    }
}
