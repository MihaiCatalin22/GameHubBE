package com.gamehub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {

    @NotBlank(message = "Username must not be empty.")
    private String username;

    @NotBlank(message = "Password must not be empty.")
    private String password;
}
