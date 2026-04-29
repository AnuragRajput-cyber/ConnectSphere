package com.connectsphere.auth.oauth;

import com.connectsphere.auth.entity.AuthProvider;

public record OAuthUserProfile(
        AuthProvider provider,
        String providerUserId,
        String email,
        String username,
        String fullName,
        String profilePicUrl
) {
}
