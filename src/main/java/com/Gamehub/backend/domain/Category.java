package com.Gamehub.backend.domain;
import jakarta.persistence.Table;
@Table(name = "categories")
public enum Category {
    GENERAL,
    QUESTIONS,
    ANNOUNCEMENTS,
    GUIDE,
    BUGS_AND_GLITCHES
}
