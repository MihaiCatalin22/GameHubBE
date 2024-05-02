package com.gamehub.backend.controller;

import com.gamehub.backend.business.UserService;
import com.gamehub.backend.business.impl.FileStorageService;
import com.gamehub.backend.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/users")
public class FileStorageController {

    private FileStorageService fileStorageService;
    private UserService userService;

    @Autowired
    public FileStorageController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    @PostMapping("/upload-profile-picture/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        try {
            UserDTO user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            String oldFileName = user.getProfilePicture();
            String fileName;

            if (oldFileName == null || oldFileName.isEmpty()) {
                fileName = fileStorageService.storeFile(file, userId);
            } else {
                fileName = fileStorageService.replaceFile(file, userId, oldFileName);
            }

            userService.updateUserProfilePicture(userId, fileName);
            UserDTO updatedUser = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found after updating profile picture"));

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not upload the file: " + e.getMessage());
        }
    }
}

