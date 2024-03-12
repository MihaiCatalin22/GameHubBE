package com.Gamehub.backend.persistence;
import com.Gamehub.backend.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
