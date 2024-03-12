package com.Gamehub.backend.business;


import com.Gamehub.backend.domain.Comment;
import com.Gamehub.backend.domain.ForumPost;

import java.util.List;
import java.util.Optional;

public interface ForumService {
    ForumPost createPost(ForumPost post);
    Optional<ForumPost> getPostById(Long id);
    List<ForumPost> getAllPosts();
    ForumPost updatePost(Long id, ForumPost post);
    void deletePost(Long id);
    void likePost(Long postId, Long userId);
    Comment commentOnPost(Long postId, Comment comment, Long userId);
}
