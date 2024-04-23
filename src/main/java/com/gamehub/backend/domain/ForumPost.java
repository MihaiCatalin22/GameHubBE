package com.gamehub.backend.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    private String title;

    @Lob
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
    private Category category;
}

