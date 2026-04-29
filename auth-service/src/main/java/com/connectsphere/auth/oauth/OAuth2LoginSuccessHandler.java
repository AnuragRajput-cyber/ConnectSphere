package com.connectsphere.auth.oauth;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.entity.AuthProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthAccountService oauthAccountService;
    private final ObjectMapper objectMapper;
    private final String successRedirectUrl;

    public OAuth2LoginSuccessHandler(
            OAuthAccountService oauthAccountService,
            ObjectMapper objectMapper,
            @Value("${app.oauth.success-redirect-url}") String successRedirectUrl
    ) {
        this.oauthAccountService = oauthAccountService;
        this.objectMapper = objectMapper;
        this.successRedirectUrl = successRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            throw new ServletException("Unexpected authentication type for OAuth login.");
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();
        OAuthUserProfile profile = buildProfile(oauthToken.getAuthorizedClientRegistrationId(), oauthUser.getAttributes());
        AuthResponse authResponse = oauthAccountService.loginOrCreateUser(profile);
        response.sendRedirect(buildSuccessRedirect(authResponse));
    }

    private OAuthUserProfile buildProfile(String registrationId, Map<String, Object> attributes) {
        AuthProvider provider = switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "github" -> AuthProvider.GITHUB;
            default -> throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "Unsupported OAuth provider: " + registrationId
            );
        };

        String providerUserId = String.valueOf(attributes.getOrDefault("id", attributes.get("sub")));
        String email = stringValue(attributes.get("email"));
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email"),
                    "OAuth provider did not return an email address."
            );
        }

        String username = stringValue(firstNonNull(attributes.get("login"), attributes.get("preferred_username"), attributes.get("name")));
        String fullName = stringValue(firstNonNull(attributes.get("name"), attributes.get("login"), attributes.get("email")));
        String profilePicUrl = stringValue(firstNonNull(attributes.get("avatar_url"), attributes.get("picture")));

        return new OAuthUserProfile(provider, providerUserId, email, username, fullName, profilePicUrl);
    }

    private Object firstNonNull(Object... values) {
        for (Object value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private String buildSuccessRedirect(AuthResponse authResponse) throws IOException {
        String encodedUser = Base64.getUrlEncoder().withoutPadding().encodeToString(
                objectMapper.writeValueAsBytes(authResponse.user())
        );
        return successRedirectUrl
                + "?accessToken=" + encode(authResponse.accessToken())
                + "&refreshToken=" + encode(authResponse.refreshToken())
                + "&accessTokenExpiresAt=" + encode(authResponse.accessTokenExpiresAt().toString())
                + "&refreshTokenExpiresAt=" + encode(authResponse.refreshTokenExpiresAt().toString())
                + "&user=" + encode(encodedUser);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
