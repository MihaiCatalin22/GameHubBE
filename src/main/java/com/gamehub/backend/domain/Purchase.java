package com.gamehub.backend.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference(value = "user-purchase")
    @NotNull(message = "User cannot be null")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_id")
    @JsonBackReference(value = "game-purchase")
    @NotNull(message = "Game cannot be null")
    private Game game;

    @NotNull(message = "Purchase date cannot be null")
    private Date purchaseDate;

    @Min(value = 0, message = "Amount must be positive")
    private Double amount;
}
