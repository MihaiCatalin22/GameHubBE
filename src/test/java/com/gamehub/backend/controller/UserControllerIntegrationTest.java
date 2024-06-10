package com.gamehub.backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.domain.FriendRelationship;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.UserDTO;
import com.gamehub.backend.persistence.FriendRelationshipRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private FriendRelationshipRepository friendRelationshipRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private User user;
    private User friend;
    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        friendRelationshipRepository.deleteAll();

        user = new User();
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setPasswordHash("password");
        user = userRepository.save(user);

        friend = new User();
        friend.setUsername("friend");
        friend.setEmail("friend@example.com");
        friend.setPasswordHash(passwordEncoder.encode("password"));
        friend = userRepository.save(friend);

        setupAuthentication(user, "ADMINISTRATOR");
    }

    private void setupAuthentication(User user, String role) {
        CustomUserDetails customUserDetails = new CustomUserDetails(
                user.getId(), user.getUsername(), user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(role)));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Debug statement
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Setup Authentication - Principal ID: " + principal.getId());
    }

    @Test
    void createUserTest() throws Exception {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", "newUser");
        newUser.put("email", "newuser@example.com");
        newUser.put("password", "securePassword123@");
        newUser.put("confirmPassword", "securePassword123@");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.jwt").exists());
    }


    @Test
    void getAllUsersTest() throws Exception {
        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserByIdTest() throws Exception {
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setEmail("test@example.com");
        newUser.setPasswordHash("password");
        newUser.setDescription("Test description");
        userRepository.save(newUser);

        mockMvc.perform(get("/users/" + newUser.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(newUser.getId()))
                .andExpect(jsonPath("$.username").value(newUser.getUsername()))
                .andExpect(jsonPath("$.email").value(newUser.getEmail()))
                .andExpect(jsonPath("$.description").value(newUser.getDescription()));
    }


    @Test
    void updateUserTest() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("originalUser");
        existingUser.setEmail("original@example.com");
        existingUser.setDescription("Original description");
        existingUser.setPasswordHash(passwordEncoder.encode("testPassword"));

        existingUser = userRepository.save(existingUser);

        CustomUserDetails customUserDetails = new CustomUserDetails(
                existingUser.getId(), existingUser.getUsername(), existingUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR")));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDTO updatedUser = new UserDTO();
        updatedUser.setUsername("updatedUser");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setDescription("Updated description");
        updatedUser.setPassword("newUpdatedPassword#");
        updatedUser.setConfirmPassword("newUpdatedPassword#");

        mockMvc.perform(put("/users/" + existingUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.description").value("Updated description"));

        User updatedUserFromDb = userRepository.findById(existingUser.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("newUpdatedPassword#", updatedUserFromDb.getPasswordHash()));
    }



    @Test
    void deleteUserTest() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void loginUserTest() throws Exception {
        String rawPassword = "Password123@";
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setEmail("email@test.com");
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

    @Test
    void sendRequestTest() throws Exception {
        setupAuthentication(user, "USER");

        mockMvc.perform(post("/users/friend-requests/send")
                        .param("userId", user.getId().toString())
                        .param("friendId", friend.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(FriendRelationship.Status.PENDING.name()));
    }

    @Test
    void createUserWithExistingUsernameTest() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("securePassword123@");
        userDTO.setConfirmPassword("securePassword123@");
        userDTO.setEmail("anotheremail@example.com");
        userDTO.setUsername("admin");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void createUserWithInvalidPasswordFormatTest() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("password");
        userDTO.setConfirmPassword("password");
        userDTO.setEmail("newuser@example.com");
        userDTO.setUsername("newUser");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void loginUserInvalidCredentialsTest() throws Exception {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "wrongPassword");

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void sendFriendRequestAlreadyExistsTest() throws Exception {
        setupAuthentication(user, "USER");

        mockMvc.perform(post("/users/friend-requests/send")
                        .param("userId", user.getId().toString())
                        .param("friendId", friend.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users/friend-requests/send")
                        .param("userId", user.getId().toString())
                        .param("friendId", friend.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Friend request already sent or user is already your friend"));
    }
}


