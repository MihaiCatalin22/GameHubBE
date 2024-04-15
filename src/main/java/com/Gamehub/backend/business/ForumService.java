package com.Gamehub.backend.business;


import com.Gamehub.backend.DTO.CommentDTO;
import com.Gamehub.backend.DTO.ForumPostResponse;
import com.Gamehub.backend.domain.Comment;
import com.Gamehub.backend.domain.ForumPost;

import java.util.List;
import java.util.Optional;

public interface ForumService {
    ForumPostResponse createPost(ForumPost post, Long userId);
    Optional<ForumPostResponse> getPostById(Long id);
    List<ForumPost> getAllPosts();
    ForumPost updatePost(Long id, ForumPost post);
    void deletePost(Long id);
    void likePost(Long postId, Long userId);
    Comment commentOnPost(Long postId, Comment comment, Long userId);
    List<CommentDTO> getCommentsByPostId(Long postId);
}
