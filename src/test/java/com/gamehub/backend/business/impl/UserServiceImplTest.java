package com.gamehub.backend.business.impl;

import com.gamehub.backend.domain.FriendRelationship;
import com.gamehub.backend.domain.Role;
import com.gamehub.backend.dto.FriendRequestDTO;
import com.gamehub.backend.dto.UserDTO;
import com.gamehub.backend.configuration.security.token.JwtUtil;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.FriendRelationshipRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FriendRelationshipRepository friendRelationshipRepository;
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
    private User friend;
    private FriendRelationship friendRelationship;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setProfilePicture("oldPicture.jpg");
        user.setRoles(Collections.singletonList(Role.USER));

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setUsername("testUser");
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("Password@123");
        userDTO.setConfirmPassword("Password@123");
        userDTO.setProfilePicture("oldPicture.jpg");
        userDTO.setRole(Collections.singletonList("USER"));

        friend = new User();
        friend.setId(2L);
        friend.setUsername("testFriend");
        friend.setEmail("friend@example.com");

        friendRelationship = new FriendRelationship();
        friendRelationship.setId(1L);
        friendRelationship.setUser(user);
        friendRelationship.setFriend(friend);
        friendRelationship.setStatus(FriendRelationship.Status.PENDING);

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
            dto.setRole(model.getRoles().stream().map(Role::name).toList());
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
    void createUser_validData_success() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO createdUserDTO = userService.createUser(userDTO);
        assertNotNull(createdUserDTO);
        assertEquals("dummyToken", createdUserDTO.getJwt());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_existingUsername_throwsException() {
        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDTO);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_existingEmail_throwsException() {
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDTO);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void createUser_weakPassword_throwsException() {
        userDTO.setPassword("weakpass");
        userDTO.setConfirmPassword("weakpass");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDTO);
        });

        assertEquals("Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
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
    void updateUser_passwordChange() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userDTO.setPassword("NewPassword@123");
        userDTO.setConfirmPassword("NewPassword@123");
        when(passwordEncoder.matches("NewPassword@123", "hashedPassword")).thenReturn(false);

        UserDTO updatedUserDTO1 = userService.updateUser(1L, userDTO);
        assertNotNull(updatedUserDTO1);
        assertEquals(user.getId(), updatedUserDTO1.getId());
        verify(passwordEncoder).encode("NewPassword@123");
        verify(userRepository).save(user);

        reset(passwordEncoder, userRepository);

        userDTO.setPassword("Password@123");
        userDTO.setConfirmPassword("Password@123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.matches("Password@123", "hashedPassword")).thenReturn(true);

        UserDTO updatedUserDTO2 = userService.updateUser(1L, userDTO);
        assertNotNull(updatedUserDTO2);
        assertEquals(user.getId(), updatedUserDTO2.getId());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository).save(user);
    }
    @Test
    void updateUser_roleChange() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userDTO.setPassword("Password@123");
        userDTO.setConfirmPassword("Password@123");
        userDTO.setRole(Arrays.asList("ADMINISTRATOR", "USER"));
        UserDTO updatedUserDTO = userService.updateUser(1L, userDTO);
        assertNotNull(updatedUserDTO);
        assertEquals(user.getId(), updatedUserDTO.getId());
        assertEquals(Arrays.asList("ADMINISTRATOR", "USER"), updatedUserDTO.getRole());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(1L, userDTO);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
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
        verify(passwordEncoder).matches("Password@123", "hashedPassword");
    }

    @Test
    void loginWithInvalidCredentials() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "hashedPassword")).thenReturn(false);

        Optional<UserDTO> result = userService.login(userDTO);

        assertTrue(result.isEmpty());
        verify(passwordEncoder).matches("Password@123", "hashedPassword");
    }

    @Test
    void updateUserProfilePicture_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updateUserProfilePicture(1L, "newPicture.jpg");
        assertEquals("newPicture.jpg", user.getProfilePicture());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfilePicture_userNotFound_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.updateUserProfilePicture(1L, "newPicture.jpg");
        });

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void sendRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRelationshipRepository.save(any(FriendRelationship.class))).thenReturn(friendRelationship);

        FriendRelationship result = userService.sendRequest(1L, 2L);

        assertNotNull(result);
        assertEquals(FriendRelationship.Status.PENDING, result.getStatus());
        verify(friendRelationshipRepository).save(any(FriendRelationship.class));
    }

    @Test
    void sendRequest_alreadyFriendsOrPending() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRelationshipRepository.existsByUserAndFriend(any(User.class), any(User.class))).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.sendRequest(1L, 2L);
        });

        assertEquals("Friend request already sent or user is already your friend", exception.getMessage());

        reset(friendRelationshipRepository);
        when(friendRelationshipRepository.existsByUserAndFriend(any(User.class), any(User.class))).thenReturn(false);
        when(friendRelationshipRepository.existsByUserAndFriendAndStatus(any(User.class), any(User.class), eq(FriendRelationship.Status.PENDING))).thenReturn(true);

        exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.sendRequest(1L, 2L);
        });

        assertEquals("Friend request already sent or user is already your friend", exception.getMessage());
    }

    @Test
    void getPendingRequests() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(friend));
        when(friendRelationshipRepository.findByFriendAndStatus(friend, FriendRelationship.Status.PENDING))
                .thenReturn(Collections.singletonList(friendRelationship));

        List<FriendRequestDTO> result = userService.getPendingRequests(2L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(friendRelationship.getId(), result.get(0).getId());
        verify(friendRelationshipRepository).findByFriendAndStatus(friend, FriendRelationship.Status.PENDING);
    }

    @Test
    void respondToRequest() {
        when(friendRelationshipRepository.findById(1L)).thenReturn(Optional.of(friendRelationship));
        when(friendRelationshipRepository.save(any(FriendRelationship.class))).thenReturn(friendRelationship);

        FriendRelationship result = userService.respondToRequest(1L, FriendRelationship.Status.ACCEPTED);

        assertNotNull(result);
        assertEquals(FriendRelationship.Status.ACCEPTED, result.getStatus());
        verify(friendRelationshipRepository).save(friendRelationship);
    }

    @Test
    void getFriends() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(friendRelationshipRepository.findByUserAndStatusOrFriendAndStatus(user, FriendRelationship.Status.ACCEPTED, user, FriendRelationship.Status.ACCEPTED))
                .thenReturn(Collections.singletonList(friendRelationship));

        List<FriendRequestDTO> result = userService.getFriends(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(friendRelationship.getId(), result.get(0).getId());
        verify(friendRelationshipRepository).findByUserAndStatusOrFriendAndStatus(user, FriendRelationship.Status.ACCEPTED, user, FriendRelationship.Status.ACCEPTED);
    }

    @Test
    void removeFriend() {
        when(friendRelationshipRepository.findById(1L)).thenReturn(Optional.of(friendRelationship));

        doNothing().when(friendRelationshipRepository).delete(any(FriendRelationship.class));

        userService.removeFriend(1L);

        verify(friendRelationshipRepository).delete(friendRelationship);
    }
}


