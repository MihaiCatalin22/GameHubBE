package com.gamehub.backend.controller;

import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.dto.CommentDTO;
import com.gamehub.backend.dto.ForumPostResponse;
import com.gamehub.backend.business.ForumService;
import com.gamehub.backend.domain.Comment;
import com.gamehub.backend.domain.ForumPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/forum")
@Validated
public class ForumController {
    private final ForumService forumService;

    @Autowired
    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumPostResponse> createPost(@Valid @RequestBody ForumPost post, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User details not found");
        }
        ForumPostResponse postResponse = forumService.createPost(post, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
    }

    @GetMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumPostResponse> getPostById(@PathVariable Long id) {
        return forumService.getPostById(id)
                .map(post -> ResponseEntity.ok().body(post))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ForumPostResponse>> getAllPosts() {
        List<ForumPostResponse> posts = forumService.getAllPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/posts/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ForumPostResponse>> getPostsByUserId(@PathVariable Long userId) {
        List<ForumPostResponse> posts = forumService.getPostsByUserId(userId);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/posts/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR') or #post.author.id == principal.id")
    public ResponseEntity<ForumPostResponse> updatePost(@PathVariable Long id, @Valid @RequestBody ForumPost post, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            ForumPostResponse updatedPost = forumService.updatePost(id, post, currentUser.getId());
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        forumService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            forumService.likePost(postId, currentUser.getId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentDTO> addCommentToPost(@PathVariable Long postId, @Valid @RequestBody Comment comment, @AuthenticationPrincipal CustomUserDetails currentUser) {
        try {
            CommentDTO createdComment = forumService.commentOnPost(postId, comment, currentUser.getId());
            return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        forumService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        try {
            List<CommentDTO> comments = forumService.getCommentsByPostId(postId);
            return ResponseEntity.ok(comments);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
