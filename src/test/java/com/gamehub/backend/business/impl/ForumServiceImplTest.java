package com.gamehub.backend.business.impl;

import com.gamehub.backend.dto.CommentDTO;
import com.gamehub.backend.domain.ForumPost;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.domain.Comment;
import com.gamehub.backend.dto.ForumPostResponse;
import com.gamehub.backend.persistence.ForumPostRepository;
import com.gamehub.backend.persistence.CommentRepository;
import com.gamehub.backend.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class ForumServiceImplTest {

    @Mock
    private ForumPostRepository forumPostRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ForumServiceImpl forumService;

    private ForumPost forumPost;
    private User user;
    private Comment comment;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("TestUser");

        forumPost = new ForumPost();
        forumPost.setId(1L);
        forumPost.setAuthor(user);
        forumPost.setTitle("Sample Post");
        forumPost.setContent("Content of the sample post");

        comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(user);
        comment.setContent("Sample comment");
        comment.setForumPost(forumPost);
        forumPost.setComments(new HashSet<>());

        comment = new Comment();
        comment.setId(1L);
        comment.setAuthor(user);
        comment.setContent("Sample comment");
        comment.setForumPost(forumPost);
        forumPost.getComments().add(comment);
    }

    @Test
    void createPost() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(forumPostRepository.save(any(ForumPost.class))).thenReturn(forumPost);
        ForumPostResponse createdPost = forumService.createPost(forumPost, user.getId());

        assertNotNull(createdPost);
        assertEquals(forumPost.getId(), createdPost.getId());
        assertEquals(forumPost.getTitle(), createdPost.getTitle());
        verify(forumPostRepository).save(any(ForumPost.class));
    }

    @Test
    void createPostWithInvalidUserId() {
        Long invalidUserId = 999L;
        ForumPost postToCreate = new ForumPost();
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            forumService.createPost(postToCreate, invalidUserId);
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void getPostById() {
        when(forumPostRepository.findById(1L)).thenReturn(Optional.of(forumPost));
        Optional<ForumPostResponse> foundPost = forumService.getPostById(1L);

        assertTrue(foundPost.isPresent());
        assertEquals(forumPost.getId(), foundPost.get().getId());
        verify(forumPostRepository).findById(1L);
    }

    @Test
    void getNonexistentPostById() {
        Long nonexistentPostId = 999L;
        when(forumPostRepository.findById(nonexistentPostId)).thenReturn(Optional.empty());

        Optional<ForumPostResponse> result = forumService.getPostById(nonexistentPostId);

        assertFalse(result.isPresent());
    }

    @Test
    void getAllPosts() {
        when(forumPostRepository.findAll()).thenReturn(Arrays.asList(forumPost));
        List<ForumPost> posts = forumService.getAllPosts();

        assertFalse(posts.isEmpty());
        assertEquals(1, posts.size());
        verify(forumPostRepository).findAll();
    }

    @Test
    void getAllPostsWhenNoneExist() {
        when(forumPostRepository.findAll()).thenReturn(Arrays.asList());

        List<ForumPost> result = forumService.getAllPosts();

        assertTrue(result.isEmpty());
    }

    @Test
    void updatePost() {
        ForumPost updatedPostData = new ForumPost();
        updatedPostData.setTitle("Updated Title");
        updatedPostData.setContent("Updated content.");

        when(forumPostRepository.findById(1L)).thenReturn(Optional.of(forumPost));
        when(forumPostRepository.save(any(ForumPost.class))).thenReturn(forumPost);

        ForumPost updatedPost = forumService.updatePost(1L, updatedPostData);
        assertNotNull(updatedPost);
        assertEquals("Updated Title", updatedPost.getTitle());
        assertEquals("Updated content.", updatedPost.getContent());
        verify(forumPostRepository).save(forumPost);
    }

    @Test
    void updateNonexistentPost() {
        Long nonexistentPostId = 999L;
        ForumPost updatedData = new ForumPost();

        when(forumPostRepository.findById(nonexistentPostId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            forumService.updatePost(nonexistentPostId, updatedData);
        });

        assertTrue(exception.getMessage().contains("Forum post not found"));
    }

    @Test
    void deletePost() {
        doNothing().when(forumPostRepository).deleteById(1L);

        forumService.deletePost(1L);

        verify(forumPostRepository).deleteById(1L);
    }

    @Test
    void deleteNonexistentPost() {
        Long nonexistentPostId = 999L;
        doThrow(new RuntimeException("Post not found.")).when(forumPostRepository).deleteById(nonexistentPostId);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            forumService.deletePost(nonexistentPostId);
        });

        assertTrue(exception.getMessage().contains("Post not found"));
    }

    @Test
    void likePost() {
        forumPost.setLikesCount(0);
        when(forumPostRepository.findById(1L)).thenReturn(Optional.of(forumPost));
        when(forumPostRepository.save(any(ForumPost.class))).thenAnswer(invocation -> invocation.getArgument(0));

        forumService.likePost(1L, 1L);
        assertEquals(1, forumPost.getLikesCount(), "The likes count should be incremented by 1.");
        verify(forumPostRepository).save(forumPost);
    }

    @Test
    void likeNonexistentPost() {
        Long nonexistentPostId = 999L;
        Long userId = 1L;
        when(forumPostRepository.findById(nonexistentPostId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            forumService.likePost(nonexistentPostId, userId);
        });

        assertTrue(exception.getMessage().contains("Post not found"));
    }

    @Test
    void commentOnPost() {
        when(forumPostRepository.findById(1L)).thenReturn(Optional.of(forumPost));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment createdComment = forumService.commentOnPost(1L, comment, 1L);

        assertNotNull(createdComment);
        assertEquals(comment.getId(), createdComment.getId());
        verify(commentRepository).save(any(Comment.class));
    }
    @Test
    void commentOnNonexistentPost() {
        Long nonExistentPostId = 999L;
        when(forumPostRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            forumService.commentOnPost(nonExistentPostId, comment, 1L);
        });

        assertTrue(exception.getMessage().contains("Post not found"));
    }
    @Test
    void getCommentsByExistingPostId() {
        when(forumPostRepository.findById(1L)).thenReturn(Optional.of(forumPost));

        List<CommentDTO> comments = forumService.getCommentsByPostId(1L);

        assertFalse(comments.isEmpty());
        assertEquals(1, comments.size());
        assertEquals(comment.getContent(), comments.get(0).getContent());
    }
    @Test
    void getCommentsByNonexistentPostId() {
        Long nonExistentPostId = 999L;
        when(forumPostRepository.findById(nonExistentPostId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            forumService.getCommentsByPostId(nonExistentPostId);
        });

        assertTrue(exception.getMessage().contains("Post not found with id: " + nonExistentPostId));
    }

}