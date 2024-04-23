package com.gamehub.backend.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String description;

    @ElementCollection(targetClass = Genre.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "game_genres", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "genre")
    private Set<Genre> genres = new HashSet<>();

    @Temporal(TemporalType.DATE)
    private Date releaseDate;

    private String developer;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @JsonManagedReference (value = "game-review")
    private List<Review> reviews = new ArrayList<>();
}
