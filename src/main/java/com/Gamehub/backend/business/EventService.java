package com.Gamehub.backend.business;

import com.Gamehub.backend.domain.Event;
import com.Gamehub.backend.domain.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventService {
    Event createEvent(Event event);
    Optional<Event> getEventById(Long id);
    List<Event> getAllEvents();
    Event updateEvent(Long id, Event event);
    void deleteEvent(Long id);
    Event addParticipant(Long eventId, Long userId);
    Event removeParticipant(Long eventId, Long userId);
    Set<User> getParticipants(Long eventId);
}
