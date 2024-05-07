package com.gamehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.domain.*;
import com.gamehub.backend.persistence.CommentRepository;
import com.gamehub.backend.persistence.ForumPostRepository;
import com.gamehub.backend.persistence.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import org.springframework.http.MediaType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.Collections;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ForumControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private User testUser;

    @BeforeEach
    void setup() {
        commentRepository.deleteAllInBatch();
        forumPostRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPasswordHash(passwordEncoder.encode("securePass123"));
        userRepository.save(testUser);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR"));
        CustomUserDetails userDetails = new CustomUserDetails(testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username="testUser", roles={"ADMINISTRATOR"})
    void createForumPostTest() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPasswordHash(passwordEncoder.encode("securePass123"));
        userRepository.save(testUser);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR"));
        CustomUserDetails userDetails = new CustomUserDetails(testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        ForumPost newPost = new ForumPost();
        newPost.setTitle("New Game Review");
        newPost.setContent("This is a review of the latest game.");
        newPost.setCategory(Category.GENERAL);
        newPost.setAuthor(testUser);

        mockMvc.perform(post("/forum/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Game Review")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void getForumPostByIdTest() throws Exception {
        ForumPost post = new ForumPost();
        post.setTitle("Existing Post");
        post.setContent("Content of the existing post.");
        post.setAuthor(testUser);
        forumPostRepository.save(post);

        mockMvc.perform(get("/forum/posts/" + post.getId())
                        .with(user("testUser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Existing Post")));
    }

    @Test
    void getAllPostsTest() throws Exception {
        ForumPost post1 = new ForumPost();
        post1.setTitle("First Post");
        post1.setContent("First post content.");
        post1.setAuthor(testUser);
        forumPostRepository.save(post1);

        ForumPost post2 = new ForumPost();
        post2.setTitle("Second Post");
        post2.setContent("Second post content.");
        post2.setAuthor(testUser);
        forumPostRepository.save(post2);

        mockMvc.perform(get("/forum/posts")
                        .with(user("testUser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @Test
    void updateForumPostTest() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPasswordHash(passwordEncoder.encode("securePass123"));
        testUser = userRepository.save(testUser);

        CustomUserDetails customUserDetails = new CustomUserDetails(
                testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR")));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ForumPost post = new ForumPost();
        post.setTitle("Original Title");
        post.setContent("Original content.");
        post.setCategory(Category.GENERAL);
        post.setAuthor(testUser);
        post = forumPostRepository.save(post);

        post.setTitle("Updated Title");
        post.setContent("Updated content.");

        mockMvc.perform(put("/forum/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post))
                        .with(user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));

        SecurityContextHolder.clearContext();
    }

    @Test
    void deleteForumPostTest() throws Exception {
        User testUser = new User();
        testUser.setUsername("testAdmin");
        testUser.setPasswordHash(passwordEncoder.encode("securePassword"));
        userRepository.save(testUser);

        CustomUserDetails customUserDetails = new CustomUserDetails(
                testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR")));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ForumPost post = new ForumPost();
        post.setTitle("To be deleted");
        post.setContent("This post will be deleted.");
        post.setAuthor(testUser);
        post = forumPostRepository.save(post);

        mockMvc.perform(delete("/forum/posts/" + post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(customUserDetails)))
                .andExpect(status().isNoContent());

        SecurityContextHolder.clearContext();
    }

    @Test
    void likeAndUnlikePostTest() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPasswordHash(passwordEncoder.encode("password"));
        userRepository.save(testUser);

        CustomUserDetails customUserDetails = new CustomUserDetails(
                testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("USER")));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ForumPost post = new ForumPost();
        post.setTitle("Like this post");
        post.setContent("You should like and then unlike this post.");
        post.setAuthor(testUser);
        post = forumPostRepository.save(post);

        mockMvc.perform(post("/forum/posts/" + post.getId() + "/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(customUserDetails)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/forum/posts/" + post.getId() + "/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(customUserDetails)))
                .andExpect(status().isOk());

        SecurityContextHolder.clearContext();
    }

    @Test
    void addCommentToPostTest() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPasswordHash(passwordEncoder.encode("securePass123"));
        testUser = userRepository.save(testUser);

        CustomUserDetails customUserDetails = new CustomUserDetails(
                testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("USER")));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ForumPost post = new ForumPost();
        post.setTitle("Post for commenting");
        post.setContent("Post content here.");
        post.setAuthor(testUser);
        post = forumPostRepository.save(post);

        Comment newComment = new Comment();
        newComment.setContent("This is a comment.");
        newComment.setAuthor(testUser);

        mockMvc.perform(post("/forum/posts/" + post.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newComment))
                        .with(user(customUserDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", is("This is a comment.")));

        SecurityContextHolder.clearContext();
    }
    @Test
    void getPostsByUserIdTest() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setPasswordHash(passwordEncoder.encode("securePass123"));
        testUser = userRepository.save(testUser);

        ForumPost post1 = new ForumPost();
        post1.setTitle("User's First Post");
        post1.setContent("Content of user's first post.");
        post1.setAuthor(testUser);
        forumPostRepository.save(post1);

        ForumPost post2 = new ForumPost();
        post2.setTitle("User's Second Post");
        post2.setContent("Content of user's second post.");
        post2.setAuthor(testUser);
        forumPostRepository.save(post2);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        CustomUserDetails userDetails = new CustomUserDetails(testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(), authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/forum/posts/user/" + testUser.getId())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].title", is("User's First Post")))
                .andExpect(jsonPath("$[1].title", is("User's Second Post")));

        SecurityContextHolder.clearContext();
    }
}
