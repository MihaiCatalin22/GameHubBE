package com.gamehub.backend.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 100, message = "Title cannot be longer than 100 characters")
    private String title;

    @Lob
    @Size(max = 1500, message = "Description cannot be longer than 1500 characters")
    private String description;

    @ElementCollection(targetClass = Genre.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "game_genres", joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "genre")
    private Set<Genre> genres = new HashSet<>();

    @Temporal(TemporalType.DATE)
    @NotNull(message = "Release date cannot be null")
    private Date releaseDate;

    @NotBlank(message = "Developer cannot be empty")
    private String developer;

    @Positive(message = "Price must be positive")
    private Double price;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @JsonManagedReference (value = "game-review")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "game-purchase")
    private List<Purchase> purchases = new ArrayList<>();
}
