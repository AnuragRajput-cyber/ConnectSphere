package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.AuthProvider;
import com.connectsphere.auth.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) 
        String username,
        @NotBlank @Email 
        String email,
        @NotBlank @Size(min = 8, max = 100) 
        String password,
        @NotBlank @Size(max = 100)
        String fullName,
        @Size(max = 500)
        String bio,
        @Size(max = 2048) String profilePicUrl,
        @NotNull Role role,
        @NotNull AuthProvider provider
) {
}
