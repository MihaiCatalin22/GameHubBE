package com.gamehub.backend.business.impl;

import com.gamehub.backend.dto.CommentDTO;
import com.gamehub.backend.dto.ForumPostResponse;
import com.gamehub.backend.dto.AuthorInfo;
import com.gamehub.backend.business.ForumService;
import com.gamehub.backend.domain.ForumPost;
import com.gamehub.backend.domain.Comment;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.ForumPostRepository;
import com.gamehub.backend.persistence.CommentRepository;
import com.gamehub.backend.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        post.setAuthor(user);
        ForumPost createdPost = forumPostRepository.save(post);
        return toForumPostResponse(createdPost);
    }

    @Override
    public Optional<ForumPostResponse> getPostById(Long id) {
        return forumPostRepository.findById(id).map(this::toForumPostResponse);
    }

    @Override
    public List<ForumPostResponse> getAllPosts() {
        return forumPostRepository.findAll().stream().map(this::toForumPostResponse).toList();
    }

    @Override
    public ForumPostResponse updatePost(Long id, ForumPost post, Long userId) {
        ForumPost existingPost = forumPostRepository.findById(id).orElseThrow(() -> new RuntimeException("Forum post not found"));
        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        forumPostRepository.save(existingPost);
        return toForumPostResponse(existingPost);
    }

    @Override
    public void deletePost(Long id) {
        forumPostRepository.deleteById(id);
    }

    @Override
    public void likePost(Long postId, Long userId) {
        ForumPost post = forumPostRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (post.isUserLiked(userId)) {
            post.removeLike(userId);
        } else {
            post.addLike(userId);
        }
        forumPostRepository.save(post);
    }

    @Override
    public CommentDTO commentOnPost(Long postId, Comment comment, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        ForumPost post = forumPostRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        comment.setAuthor(user);
        comment.setForumPost(post);
        Comment savedComment = commentRepository.save(comment);
        return convertToCommentDTO(savedComment);
    }

    @Override
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        Set<Comment> comments = forumPostRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found")).getComments();
        return comments.stream().map(this::convertToCommentDTO).toList();
    }

    @Override
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!comment.getForumPost().getId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to the specified post");
        }
        commentRepository.delete(comment);
    }

    private ForumPostResponse toForumPostResponse(ForumPost post) {
        List<CommentDTO> commentDTOs = post.getComments().stream().map(this::convertToCommentDTO).toList();
        AuthorInfo authorInfo = new AuthorInfo(post.getAuthor().getId(), post.getAuthor().getUsername());
        return new ForumPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                authorInfo,
                post.getCreationDate(),
                post.getLikesCount(),
                post.getCategory() != null ? post.getCategory().name() : null,
                commentDTOs
        );
    }

    private CommentDTO convertToCommentDTO(Comment comment) {
        AuthorInfo authorInfo = new AuthorInfo(comment.getAuthor().getId(), comment.getAuthor().getUsername());
        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                authorInfo,
                comment.getCreationDate()
        );
    }
}
