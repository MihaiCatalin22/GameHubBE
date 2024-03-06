package com.Gamehub.backend.business.impl;

import com.Gamehub.backend.domain.User;
import com.Gamehub.backend.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
    }

    @Test
    void createUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User createdUser = userService.createUser(new User());
        assertNotNull(createdUser);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUserById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals(user.getId(), foundUser.get().getId());
    }

    @Test
    void getAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        List<User> users = userService.getAllUsers();
        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User updatedUser = userService.updateUser(1L, new User());
        assertNotNull(updatedUser);
        assertEquals(user.getId(), updatedUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }
}