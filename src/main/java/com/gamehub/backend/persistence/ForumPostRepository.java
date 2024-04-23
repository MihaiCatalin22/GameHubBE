package com.gamehub.backend.persistence;
import com.gamehub.backend.domain.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
}
