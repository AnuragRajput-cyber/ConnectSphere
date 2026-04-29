package com.connectsphere.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 500) String bio,
        @Size(max = 500) String profilePicUrl,
        @Size(max = 500) String bannerUrl,
        boolean privateAccount
) {
}
