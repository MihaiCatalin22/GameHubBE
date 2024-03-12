package com.Gamehub.backend.persistence;
import com.Gamehub.backend.domain.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
}
