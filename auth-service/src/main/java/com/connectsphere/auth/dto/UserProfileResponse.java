package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.User;
import java.time.Instant;

public record UserProfileResponse(
        String userId,
        String username,
        String email,
        String fullName,
        String bio,
        String profilePicUrl,
        String bannerUrl,
        boolean privateAccount,
        String role,
        String provider,
        boolean active,
        Instant createdAt
) {
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getBio(),
                user.getProfilePicUrl(),
                user.getBannerUrl(),
                user.isPrivateAccount(),
                user.getRole().name(),
                user.getProvider().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
