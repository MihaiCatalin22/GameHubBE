package com.gamehub.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "friend_relationships")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"user", "friend"})
public class FriendRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull(message = "User cannot be null")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    @NotNull(message = "Friend cannot be null")
    private User friend;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    private Status status;

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}
