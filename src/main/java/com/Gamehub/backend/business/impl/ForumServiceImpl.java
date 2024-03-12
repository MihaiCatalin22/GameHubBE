package com.Gamehub.backend.business.impl;

import com.Gamehub.backend.DTO.ForumPostResponse;
import com.Gamehub.backend.DTO.AuthorInfo;
import com.Gamehub.backend.business.ForumService;
import com.Gamehub.backend.domain.ForumPost;
import com.Gamehub.backend.domain.Comment;
import com.Gamehub.backend.domain.User;
import com.Gamehub.backend.persistence.ForumPostRepository;
import com.Gamehub.backend.persistence.CommentRepository;
import com.Gamehub.backend.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class ForumServiceImpl implements ForumService {
    private final ForumPostRepository forumPostRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    @Autowired
    public ForumServiceImpl(ForumPostRepository forumPostRepository, CommentRepository commentRepository, UserRepository userRepository){
        this.forumPostRepository = forumPostRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }
    @Override
    public ForumPostResponse createPost(ForumPost post, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setAuthor(user);
        ForumPost createdPost = forumPostRepository.save(post);
        return toForumPostResponse(createdPost);
    }
    private ForumPostResponse toForumPostResponse(ForumPost post) {
        AuthorInfo authorInfo = new AuthorInfo(post.getAuthor().getId(), post.getAuthor().getUsername());
        ForumPostResponse response = new ForumPostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreationDate(post.getCreationDate());
        response.setLikesCount(post.getLikesCount());
        response.setCategory(post.getCategory() != null ? post.getCategory().toString() : null);
        response.setAuthor(new AuthorInfo(post.getAuthor().getId(), post.getAuthor().getUsername()));

        return response;
    }
    @Override
    public Optional<ForumPost> getPostById(Long id) {
        return forumPostRepository.findById(id);
    }

    @Override
    public List<ForumPost> getAllPosts() {
        return forumPostRepository.findAll();
    }

    @Override
    public ForumPost updatePost(Long id, ForumPost post) {
        return forumPostRepository.findById(id)
                .map(existingPost -> {
                    existingPost.setTitle(post.getTitle());
                    existingPost.setContent(post.getContent());
                    return forumPostRepository.save(existingPost);
                }).orElseThrow(() -> new RuntimeException("Forum post not found."));
    }

    @Override
    public void deletePost(Long id) {
        forumPostRepository.deleteById(id);
    }

    @Override
    public void likePost(Long postId, Long userId) {
        ForumPost post = forumPostRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found."));
        post.setLikesCount(post.getLikesCount() + 1);
        forumPostRepository.save(post);
    }

    @Override
    public Comment commentOnPost(Long postId, Comment comment, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found."));
        ForumPost post = forumPostRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found."));
        comment.setAuthor(user);
        comment.setForumPost(post);
        return commentRepository.save(comment);
    }
}
