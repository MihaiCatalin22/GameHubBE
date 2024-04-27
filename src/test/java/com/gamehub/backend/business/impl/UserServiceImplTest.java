package com.gamehub.backend.business.impl;

import com.gamehub.backend.dto.UserDTO;
import com.gamehub.backend.configuration.security.token.JwtUtil;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.UserRepository;
import com.gamehub.backend.persistence.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

    @Mock
    private UserMapper userMapper;
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

        lenient().when(userMapper.toDto(any(User.class))).thenAnswer(invocation -> {
            User model = invocation.getArgument(0);
            UserDTO dto = new UserDTO();
            dto.setId(model.getId());
            dto.setUsername(model.getUsername());
            dto.setEmail(model.getEmail());
            dto.setJwt(jwtUtil.generateToken(model.getUsername()));
            return dto;
        });

        lenient().when(userMapper.toEntity(any(UserDTO.class))).thenAnswer(invocation -> {
            UserDTO dto = invocation.getArgument(0);
            User model = new User();
            model.setId(dto.getId());
            model.setUsername(dto.getUsername());
            model.setEmail(dto.getEmail());
            model.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
            return model;
        });
    }
    @BeforeEach
    void resetMocks() {
        Mockito.reset(jwtUtil, userRepository);
        when(jwtUtil.generateToken(anyString())).thenReturn("dummyToken");
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
        when(jwtUtil.generateToken(anyString())).thenReturn("dummyToken");

        Optional<UserDTO> foundUserDTO = userService.getUserById(1L);

        assertTrue(foundUserDTO.isPresent());
        assertEquals("dummyToken", foundUserDTO.get().getJwt());
        verify(jwtUtil).generateToken(user.getUsername());
    }

    @Test
    void getAllUsers() {
        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setUsername("anotherUser");
        secondUser.setEmail("another@example.com");
        secondUser.setPasswordHash("hashedPasswordAnother");

        List<User> userList = Arrays.asList(user, secondUser);
        when(userRepository.findAll()).thenReturn(userList);
        when(jwtUtil.generateToken(user.getUsername())).thenReturn("dummyToken");
        when(jwtUtil.generateToken(secondUser.getUsername())).thenReturn("dummyToken");
        List<UserDTO> users = userService.getAllUsers();

        assertFalse(users.isEmpty(), "The user list should not be empty.");
        assertEquals(userList.size(), users.size(), "The size of the user lists should match.");

        for (UserDTO dto : users) {
            assertEquals("dummyToken", dto.getJwt(), "The JWT token should match the expected dummy token.");
        }

        verify(userRepository).findAll();
        verify(jwtUtil, times(userList.size())).generateToken(anyString());
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

