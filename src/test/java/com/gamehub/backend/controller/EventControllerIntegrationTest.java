package com.gamehub.backend.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.domain.Event;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.EventRepository;
import com.gamehub.backend.persistence.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;

    @BeforeEach
    void setup() {
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPasswordHash(passwordEncoder.encode("adminPass123"));
        userRepository.save(adminUser);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMINISTRATOR"));
        Authentication auth = new UsernamePasswordAuthenticationToken(adminUser.getUsername(), adminUser.getPasswordHash(), authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createEventTest() throws Exception {
        Event event = new Event();
        event.setName("Spring Festival");
        event.setDescription("A festival to welcome spring");
        event.setStartDate(new Date());
        event.setEndDate(new Date());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spring Festival"));
    }

    @Test
    void getEventByIdTest() throws Exception {
        Event sampleEvent = new Event();
        sampleEvent.setName("Spring Festival");
        sampleEvent.setDescription("A fun spring event for the community.");
        sampleEvent.setStartDate(new Date());
        sampleEvent.setEndDate(new Date());

        eventRepository.save(sampleEvent);

        mockMvc.perform(get("/events/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Spring Festival"));
    }

    @Test
    void getAllEventsTest() throws Exception {
        Event sampleEvent = new Event();
        sampleEvent.setName("Spring Festival");
        sampleEvent.setDescription("A fun spring event for the community.");
        sampleEvent.setStartDate(new Date());
        sampleEvent.setEndDate(new Date());

        eventRepository.save(sampleEvent);

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Spring Festival"));
    }

    @Test
    void updateEventTest() throws Exception {
        Event sampleEvent = new Event();
        sampleEvent.setName("Spring Festival");
        sampleEvent.setDescription("A fun spring event for the community.");
        sampleEvent.setStartDate(new Date());
        sampleEvent.setEndDate(new Date());

        Event savedEvent = eventRepository.save(sampleEvent);

        Event updatedEvent = new Event();
        updatedEvent.setName("Summer Festival");

        mockMvc.perform(put("/events/{id}", savedEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedEvent)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Summer Festival"));
    }

    @Test
    void deleteEventTest() throws Exception {
        mockMvc.perform(delete("/events/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void addParticipantToEventTest() throws Exception {
        Event sampleEvent = new Event();
        sampleEvent.setName("Spring Festival");
        sampleEvent.setDescription("A fun spring event for the community.");
        sampleEvent.setStartDate(new Date());
        sampleEvent.setEndDate(new Date());

        Event savedEvent = eventRepository.save(sampleEvent);

        User testUser = new User();
        testUser.setUsername("adminUser");
        testUser.setPasswordHash(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        CustomUserDetails userDetails = new CustomUserDetails(testUser.getId(), testUser.getUsername(), testUser.getPasswordHash(), authorities);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        mockMvc.perform(post("/events/{eventId}/participants", savedEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + testUser.getId() + "}"))
                .andExpect(status().isOk());

        SecurityContextHolder.clearContext();
    }

    @Test
    void removeParticipantFromEventTest() throws Exception {
        Event sampleEvent = new Event();
        sampleEvent.setName("Spring Festival");
        sampleEvent.setDescription("A fun spring event for the community.");
        sampleEvent.setStartDate(new Date());
        sampleEvent.setEndDate(new Date());

        Event savedEvent = eventRepository.save(sampleEvent);

        mockMvc.perform(delete("/events/{eventId}/participants/{userId}", savedEvent.getId(), adminUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void getParticipantsOfEventTest() throws Exception {
        Event sampleEvent = new Event();
        sampleEvent.setName("Spring Festival");
        sampleEvent.setDescription("A fun spring event for the community.");
        sampleEvent.setStartDate(new Date());
        sampleEvent.setEndDate(new Date());

        Event savedEvent = eventRepository.save(sampleEvent);

        mockMvc.perform(get("/events/{eventId}/participants", savedEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

