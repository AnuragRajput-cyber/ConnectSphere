package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.User;
import java.time.Instant;

public record PublicUserProfileResponse(
        String userId,
        String username,
        String fullName,
        String bio,
        String profilePicUrl,
        String bannerUrl,
        boolean privateAccount,
        String role,
        boolean active,
        Instant createdAt
) {
    public static PublicUserProfileResponse from(User user) {
        return new PublicUserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getBio(),
                user.getProfilePicUrl(),
                user.getBannerUrl(),
                user.isPrivateAccount(),
                user.getRole().name(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
