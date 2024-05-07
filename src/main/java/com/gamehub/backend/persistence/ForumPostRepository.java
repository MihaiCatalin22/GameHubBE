package com.gamehub.backend.persistence;
import com.gamehub.backend.domain.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByAuthorId(Long authorId);
}
