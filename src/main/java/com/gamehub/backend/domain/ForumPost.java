package com.gamehub.backend.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 100, message = "Title cannot be longer than 100 characters")
    private String title;

    @Lob
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @CreationTimestamp
    private Date creationDate;

    private long likesCount;

    @OneToMany(mappedBy = "forumPost", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Set<Comment> comments = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Category cannot be null")
    private Category category;

    @ElementCollection
    private Set<Long> likedByUsers = new HashSet<>();

    public void addLike(Long userId) {
        if (!likedByUsers.contains(userId)) {
            likedByUsers.add(userId);
            likesCount++;
        }
    }

    public void removeLike(Long userId) {
        if (likedByUsers.contains(userId)) {
            likedByUsers.remove(userId);
            likesCount--;
        }
    }

    public boolean isUserLiked(Long userId) {
        return likedByUsers.contains(userId);
    }
}

