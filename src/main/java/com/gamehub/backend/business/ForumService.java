package com.gamehub.backend.business;


import com.gamehub.backend.dto.CommentDTO;
import com.gamehub.backend.dto.ForumPostResponse;
import com.gamehub.backend.domain.Comment;
import com.gamehub.backend.domain.ForumPost;

import java.util.List;
import java.util.Optional;

public interface ForumService {
    ForumPostResponse createPost(ForumPost post, Long userId);
    Optional<ForumPostResponse> getPostById(Long id);
    List<ForumPostResponse> getAllPosts();
    ForumPostResponse updatePost(Long id, ForumPost post, Long userId);
    void deletePost(Long id);
    void likePost(Long postId, Long userId);
    CommentDTO commentOnPost(Long postId, Comment comment, Long userId);
    List<CommentDTO> getCommentsByPostId(Long postId);
    void deleteComment(Long postId, Long commentId);
}
