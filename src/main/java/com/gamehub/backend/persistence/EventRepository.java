package com.gamehub.backend.persistence;

import com.gamehub.backend.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
