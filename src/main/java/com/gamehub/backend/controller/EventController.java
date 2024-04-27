package com.gamehub.backend.controller;

import com.gamehub.backend.domain.Event;
import com.gamehub.backend.business.EventService;
import com.gamehub.backend.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/events")
public class EventController {
    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'COMMUNITY_MANAGER')")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id " + id));
        return ResponseEntity.ok(event);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'COMMUNITY_MANAGER')")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(id, eventDetails);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'COMMUNITY_MANAGER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{eventId}/participants")
    @PreAuthorize("#payload['userId'] == authentication.principal.id")
    public ResponseEntity<Event> addParticipantToEvent(@PathVariable Long eventId, @RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        Event eventWithNewParticipant = eventService.addParticipant(eventId, userId);
        return new ResponseEntity<>(eventWithNewParticipant, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}/participants/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'COMMUNITY_MANAGER') or #userId == authentication.principal.id")
    public ResponseEntity<Event> removeParticipantFromEvent(@PathVariable Long eventId, @PathVariable Long userId) {
        Event eventWithoutParticipant = eventService.removeParticipant(eventId, userId);
        return new ResponseEntity<>(eventWithoutParticipant, HttpStatus.OK);
    }

    @GetMapping("/{eventId}/participants")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'COMMUNITY_MANAGER')")
    public ResponseEntity<Set<User>> getParticipantsOfEvent(@PathVariable Long eventId) {
        Set<User> participants = eventService.getParticipants(eventId);
        return new ResponseEntity<>(participants, HttpStatus.OK);
    }
}
