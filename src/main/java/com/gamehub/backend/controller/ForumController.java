package com.gamehub.backend.controller;

import com.gamehub.backend.dto.CommentDTO;
import com.gamehub.backend.dto.ForumPostResponse;
import com.gamehub.backend.business.ForumService;
import com.gamehub.backend.domain.Comment;
import com.gamehub.backend.domain.ForumPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/forum")
public class ForumController {
    private final ForumService forumService;

    @Autowired
    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ForumPostResponse> createPost(@RequestBody ForumPost post, @RequestParam Long userId) {
        ForumPostResponse postResponse = forumService.createPost(post, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponse);
    }

    @GetMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        return forumService.getPostById(id)
                .map(post -> ResponseEntity.ok().body(post))
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ForumPost>> getAllPosts(){
        List<ForumPost> posts = forumService.getAllPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @PutMapping("/posts/{id}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<ForumPost> updatePost(@PathVariable Long id, @RequestBody ForumPost post) {
        try {
            ForumPost updatedPost = forumService.updatePost(id, post);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR') or #userId == authentication.principal.id")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        forumService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("#userId != authentication.principal.id")
    public ResponseEntity<Void> likePost(@PathVariable Long postId, @RequestParam Long userId) {
        try {
            forumService.likePost(postId, userId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> addCommentToPost(@PathVariable Long postId, @RequestBody Comment comment, @RequestParam Long userId) {
        try {
            Comment createdComment = forumService.commentOnPost(postId, comment, userId);
            return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostId(@PathVariable Long postId) {
        try {
            List<CommentDTO> comments = forumService.getCommentsByPostId(postId);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
