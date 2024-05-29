package com.gamehub.backend.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDTO {
    private Long id;
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    private String confirmPassword;

    @Email(message = "Email should be valid.")
    @NotBlank(message = "Email must not be empty.")
    private String email;

    @NotBlank(message = "Username must not be empty.")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    private String username;

    @Size(max = 500, message = "Description must be less than 500 characters.")
    private String description;

    private List<String> role;

    private String jwt;

    private String profilePicture;
}
