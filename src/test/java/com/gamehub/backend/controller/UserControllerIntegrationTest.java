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

//    @Test
//    void getPendingRequestsTest() throws Exception {
//        // Authenticate as the sender user
//        User sender = new User();
//        sender.setId(1L);
//        sender.setUsername("sender");
//        sender.setEmail("sender@example.com");
//        sender.setPasswordHash(passwordEncoder.encode("password"));
//        sender = userRepository.save(sender);
//
//        setupAuthentication(sender, "USER");
//
//        // First send a friend request
//        mockMvc.perform(post("/users/friend-requests/send")
//                        .param("userId", sender.getId().toString())
//                        .param("friendId", friend.getId().toString()))
//                .andExpect(status().isOk());
//
//        // Authenticate as the friend user
//        setupAuthentication(friend, "USER");
//
//        // Then check for pending requests
//        mockMvc.perform(get("/users/friend-requests/pending/" + friend.getId())
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$[0].user.username").value(sender.getUsername()));
//    }
//
//    @Test
//    void respondToRequestTest() throws Exception {
//        // Authenticate as the sender user
//        User sender = new User();
//        sender.setUsername("sender");
//        sender.setEmail("sender@example.com");
//        sender.setPasswordHash(passwordEncoder.encode("password"));
//        sender = userRepository.save(sender);
//
//        setupAuthentication(sender, "USER");
//
//        // First send a friend request
//        mockMvc.perform(post("/users/friend-requests/send")
//                        .param("userId", sender.getId().toString())
//                        .param("friendId", friend.getId().toString()))
//                .andExpect(status().isOk());
//
//        // Find the request ID
//        Long requestId = friendRelationshipRepository.findByFriendAndStatus(friend, FriendRelationship.Status.PENDING)
//                .get(0).getId();
//
//        // Authenticate as the friend user
//        setupAuthentication(friend, "USER");
//
//        // Respond to the friend request
//        mockMvc.perform(post("/users/friend-requests/respond")
//                        .param("relationshipId", requestId.toString())
//                        .param("userId", friend.getId().toString())
//                        .param("status", FriendRelationship.Status.ACCEPTED.name()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(FriendRelationship.Status.ACCEPTED.name()));
//    }
//
//    @Test
//    void getFriendsTest() throws Exception {
//        // Authenticate as the sender user
//        User sender = new User();
//        sender.setUsername("sender");
//        sender.setEmail("sender@example.com");
//        sender.setPasswordHash(passwordEncoder.encode("password"));
//        sender = userRepository.save(sender);
//
//        setupAuthentication(sender, "USER");
//
//        // First send and accept a friend request
//        mockMvc.perform(post("/users/friend-requests/send")
//                        .param("userId", sender.getId().toString())
//                        .param("friendId", friend.getId().toString()))
//                .andExpect(status().isOk());
//
//        Long requestId = friendRelationshipRepository.findByFriendAndStatus(friend, FriendRelationship.Status.PENDING)
//                .get(0).getId();
//
//        setupAuthentication(friend, "USER");
//
//        mockMvc.perform(post("/users/friend-requests/respond")
//                        .param("relationshipId", requestId.toString())
//                        .param("userId", friend.getId().toString())
//                        .param("status", FriendRelationship.Status.ACCEPTED.name()))
//                .andExpect(status().isOk());
//
//        setupAuthentication(sender, "USER");
//
//        // Then get the friends list
//        mockMvc.perform(get("/users/friends/" + sender.getId())
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$[0].friend.username").value(friend.getUsername()));
//    }
//
//    @Test
//    void removeFriendTest() throws Exception {
//        // Authenticate as the sender user
//        User sender = new User();
//        sender.setUsername("sender");
//        sender.setEmail("sender@example.com");
//        sender.setPasswordHash(passwordEncoder.encode("password"));
//        sender = userRepository.save(sender);
//
//        setupAuthentication(sender, "USER");
//
//        // First send and accept a friend request
//        mockMvc.perform(post("/users/friend-requests/send")
//                        .param("userId", sender.getId().toString())
//                        .param("friendId", friend.getId().toString()))
//                .andExpect(status().isOk());
//
//        Long requestId = friendRelationshipRepository.findByFriendAndStatus(friend, FriendRelationship.Status.PENDING)
//                .get(0).getId();
//
//        setupAuthentication(friend, "USER");
//
//        mockMvc.perform(post("/users/friend-requests/respond")
//                        .param("relationshipId", requestId.toString())
//                        .param("userId", friend.getId().toString())
//                        .param("status", FriendRelationship.Status.ACCEPTED.name()))
//                .andExpect(status().isOk());
//
//        setupAuthentication(sender, "USER");
//
//        // Then remove the friend
//        mockMvc.perform(delete("/users/friends/remove")
//                        .param("userId", sender.getId().toString())
//                        .param("friendId", friend.getId().toString()))
//                .andExpect(status().isOk());
//
//        // Check that the friend is removed
//        mockMvc.perform(get("/users/friends/" + sender.getId())
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$").isEmpty());
//    }
}


