package com.gamehub.backend.persistence;
import com.gamehub.backend.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
