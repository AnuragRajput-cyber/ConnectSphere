package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.User;
import java.time.Instant;

public record AdminUserResponse(
        String userId,
        String username,
        String email,
        boolean emailVerified,
        String fullName,
        String bio,
        String profilePicUrl,
        String bannerUrl,
        boolean privateAccount,
        String role,
        String provider,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.isEmailVerified(),
                user.getFullName(),
                user.getBio(),
                user.getProfilePicUrl(),
                user.getBannerUrl(),
                user.isPrivateAccount(),
                user.getRole().name(),
                user.getProvider().name(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

