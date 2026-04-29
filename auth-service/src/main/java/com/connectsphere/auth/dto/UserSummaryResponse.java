package com.connectsphere.auth.dto;

import com.connectsphere.auth.entity.User;

public record UserSummaryResponse(
        String userId,
        String username,
        String fullName,
        String profilePicUrl,
        String role
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getProfilePicUrl(),
                user.getRole().name()
        );
    }
}
