package com.gamehub.backend.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @ManyToOne
    private User author;

    @CreationTimestamp
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "forum_post_id")
    @JsonBackReference
    private ForumPost forumPost;
}
