package com.gamehub.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String message;

    private LocalDateTime timestamp;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;
    private String type;
    private Long senderId;
    private Long eventId;
}
