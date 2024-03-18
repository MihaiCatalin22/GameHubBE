package com.Gamehub.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

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

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Temporal(TemporalType.DATE)
    private Date releaseDate;

    private String developer;
}
