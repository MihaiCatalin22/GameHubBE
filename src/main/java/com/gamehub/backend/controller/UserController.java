package com.gamehub.backend.controller;

import com.gamehub.backend.configuration.security.token.JwtUtil;
import com.gamehub.backend.domain.FriendRelationship;
import com.gamehub.backend.dto.FriendRequestDTO;
import com.gamehub.backend.dto.LoginDTO;
import com.gamehub.backend.dto.UserDTO;
import com.gamehub.backend.business.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173", exposedHeaders = "Authorization")
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        try {
            return ResponseEntity.ok(userService.updateUser(id, userDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == principal.id or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(loginDTO.getUsername());
        userDTO.setPassword(loginDTO.getPassword());

        return userService.login(userDTO)
                .map(dto -> ResponseEntity.ok().header("Authorization", "Bearer " + dto.getJwt()).body(dto))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/friend-requests/send")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FriendRelationship> sendRequest(@RequestParam Long userId, @RequestParam Long friendId) {
        return ResponseEntity.ok(userService.sendRequest(userId, friendId));
    }

    @GetMapping("/friend-requests/pending/{userId}")
    @PreAuthorize("#userId == principal.id")
    public ResponseEntity<List<FriendRequestDTO>> getPendingRequests(@PathVariable Long userId) {
        List<FriendRequestDTO> pendingRequests = userService.getPendingRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/friend-requests/respond")
    @PreAuthorize("#userId == principal.id")
    public ResponseEntity<FriendRelationship> respondToRequest(@RequestParam Long relationshipId, @RequestParam Long userId, @RequestParam FriendRelationship.Status status) {
        return ResponseEntity.ok(userService.respondToRequest(relationshipId, status));
    }

    @GetMapping("/friends/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FriendRequestDTO>> getFriends(@PathVariable Long userId) {
        List<FriendRequestDTO> friends = userService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @DeleteMapping("/friends/remove/{relationshipId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFriend(@PathVariable Long relationshipId) {
        userService.removeFriend(relationshipId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-username")
    public ResponseEntity<Boolean> verifyUsername(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        boolean exists = userService.verifyUsername(username);
        return ResponseEntity.ok(exists);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");
        boolean success = userService.resetPassword(username, newPassword);
        return success ? ResponseEntity.ok("Password has been reset successfully.")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password reset failed.");
    }
}
