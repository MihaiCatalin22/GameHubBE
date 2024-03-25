package com.Gamehub.backend.controller;

import com.Gamehub.backend.domain.Event;
import com.Gamehub.backend.business.EventService;
import com.Gamehub.backend.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new RuntimeException("Event not found with id " + id));
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event eventDetails) {
        Event updatedEvent = eventService.updateEvent(id, eventDetails);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{eventId}/participants")
    public ResponseEntity<Event> addParticipantToEvent(@PathVariable Long eventId, @RequestBody Long userId) {
        Event eventWithNewParticipant = eventService.addParticipant(eventId, userId);
        return new ResponseEntity<>(eventWithNewParticipant, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<Event> removeParticipantFromEvent(@PathVariable Long eventId, @PathVariable Long userId) {
        Event eventWithoutParticipant = eventService.removeParticipant(eventId, userId);
        return new ResponseEntity<>(eventWithoutParticipant, HttpStatus.OK);
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<Set<User>> getParticipantsOfEvent(@PathVariable Long eventId) {
        Set<User> participants = eventService.getParticipants(eventId);
        return new ResponseEntity<>(participants, HttpStatus.OK);
    }
}
