package com.gamehub.backend.controller;

import com.gamehub.backend.business.NotificationService;
import com.gamehub.backend.domain.Event;
import com.gamehub.backend.business.EventService;
import com.gamehub.backend.domain.Notification;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.NotificationDTO;
import com.gamehub.backend.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/events")
@Validated
public class EventController {
    private final EventService eventService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public EventController(EventService eventService, NotificationService notificationService, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR', 'COMMUNITY_MANAGER')")
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
        Event createdEvent = eventService.createEvent(event);
        userRepository.findAll().forEach(user -> {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage("A new event has been created: " + createdEvent.getName());
            notification.setType("event");
            notification.setEventId(createdEvent.getId());
            Notification savedNotification = notificationService.save(notification);

            NotificationDTO notificationDTO = new NotificationDTO(
                    savedNotification.getId(),
                    user.getId(),
                    savedNotification.getMessage(),
                    savedNotification.getType(),
                    null,
                    createdEvent.getId()
            );

            messagingTemplate.convertAndSend("/user/" + user.getId() + "/queue/notifications", notificationDTO);
        });

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
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event eventDetails) {
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
