package com.gamehub.backend.business.impl;

import com.gamehub.backend.dto.UserDTO;
import com.gamehub.backend.configuration.security.token.JwtUtil;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password123");

        lenient().when(jwtUtil.generateToken(anyString())).thenReturn("dummyToken");
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
    }

    @Test
    void createUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDTO createdUserDTO = userService.createUser(userDTO);
        assertNotNull(createdUserDTO);
        verify(userRepository).save(any(User.class));
        assertEquals("dummyToken", createdUserDTO.getJwt());
    }

    @Test
    void getUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<UserDTO> foundUserDTO = userService.getUserById(1L);
        assertTrue(foundUserDTO.isPresent());
        assertEquals(user.getId(), foundUserDTO.get().getId());
        assertEquals("dummyToken", foundUserDTO.get().getJwt());
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        List<UserDTO> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        verify(userRepository).findAll();
        assertEquals("dummyToken", users.get(0).getJwt());
    }

    @Test
    void updateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDTO updatedUserDTO = userService.updateUser(1L, userDTO);
        assertNotNull(updatedUserDTO);
        assertEquals(user.getId(), updatedUserDTO.getId());
        verify(userRepository).save(any(User.class));
        assertEquals("dummyToken", updatedUserDTO.getJwt());
    }

    @Test
    void deleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void loginWithValidCredentials() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        Optional<UserDTO> result = userService.login(userDTO);

        assertTrue(result.isPresent());
        assertEquals(userDTO.getUsername(), result.get().getUsername());
        assertEquals("dummyToken", result.get().getJwt());
        verify(passwordEncoder).matches("password123", "hashedPassword");
    }

    @Test
    void loginWithInvalidCredentials() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        Optional<UserDTO> result = userService.login(userDTO);

        assertTrue(result.isEmpty());
        verify(passwordEncoder).matches("password123", "hashedPassword");
    }
}
