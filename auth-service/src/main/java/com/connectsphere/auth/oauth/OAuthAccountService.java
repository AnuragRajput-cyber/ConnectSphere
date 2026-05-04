package com.connectsphere.auth.oauth;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.UserProfileResponse;
import com.connectsphere.auth.entity.AuthProvider;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.exception.BadRequestException;
import com.connectsphere.auth.repository.UserRepository;
import com.connectsphere.auth.security.JwtTokenService;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OAuthAccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public OAuthAccountService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse loginOrCreateUser(OAuthUserProfile profile) {
        User user = userRepository.findByEmail(profile.email())
                .map(existingUser -> reuseExistingUser(existingUser, profile))
                .orElseGet(() -> createOAuthUser(profile));

        if (!user.isActive()) {
            throw new BadRequestException("Inactive accounts cannot sign in.");
        }

        return buildAuthResponse(user);
    }

    private User reuseExistingUser(User user, OAuthUserProfile profile) {
        if (user.getProvider() != profile.provider()) {
            throw new BadRequestException(
                    "An account with this email already exists under a different sign-in method. Use the original login method."
            );
        }

        if (isBlank(user.getFullName()) && !isBlank(profile.fullName())) {
            user.setFullName(profile.fullName().trim());
        }
        if (isBlank(user.getProfilePicUrl()) && !isBlank(profile.profilePicUrl())) {
            user.setProfilePicUrl(profile.profilePicUrl().trim());
        }

        return userRepository.save(user);
    }

    private User createOAuthUser(OAuthUserProfile profile) {
        User user = new User();
        user.setEmail(profile.email().trim().toLowerCase(Locale.ROOT));
        user.setUsername(createUniqueUsername(profile.username(), profile.fullName()));
        user.setFullName(resolveFullName(profile));
        // OAuth users do not authenticate with a local password, so we store a random placeholder hash.
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setBio(null);
        user.setProfilePicUrl(blankToNull(profile.profilePicUrl()));
        user.setRole(Role.USER);
        user.setProvider(profile.provider());
        user.setActive(true);
        return userRepository.save(user);
    }

    private String createUniqueUsername(String preferredUsername, String fullName) {
        String baseUsername = sanitizeUsername(firstNonBlank(preferredUsername, fullName, "user"));
        String candidate = baseUsername;
        int suffix = 1;

        while (userRepository.existsByUsername(candidate)) {
            String suffixText = String.valueOf(suffix);
            int maxBaseLength = Math.max(1, 50 - suffixText.length());
            String truncatedBase = baseUsername.length() > maxBaseLength ? baseUsername.substring(0, maxBaseLength) : baseUsername;
            candidate = truncatedBase + suffixText;
            suffix++;
        }

        return candidate;
    }

    private String resolveFullName(OAuthUserProfile profile) {
        return firstNonBlank(profile.fullName(), profile.username(), "ConnectSphere User").trim();
    }

    private AuthResponse buildAuthResponse(User user) {
        JwtTokenService.TokenDetails accessToken = jwtTokenService.generateAccessToken(user);
        JwtTokenService.TokenDetails refreshToken = jwtTokenService.generateRefreshToken(user);
        return new AuthResponse(
                accessToken.token(),
                refreshToken.token(),
                accessToken.expiresAt(),
                refreshToken.expiresAt(),
                UserProfileResponse.from(user)
        );
    }

    private String sanitizeUsername(String rawValue) {
        String normalized = rawValue == null ? "" : rawValue.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9._-]", "");
        if (normalized.length() < 3) {
            normalized = (normalized + "user").substring(0, 4);
        }
        return normalized.length() > 50 ? normalized.substring(0, 50) : normalized;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
