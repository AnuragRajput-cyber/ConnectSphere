package com.connectsphere.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.entity.AuthProvider;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.exception.BadRequestException;
import com.connectsphere.auth.oauth.OAuthAccountService;
import com.connectsphere.auth.oauth.OAuthUserProfile;
import com.connectsphere.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class OAuthAccountServiceTest {

    @Autowired
    private OAuthAccountService oauthAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUsers() {
        userRepository.deleteAll();
    }

    @Test
    void createsNewGithubUserAndReturnsTokens() {
        String email = "coder-" + System.nanoTime() + "@example.com";
        AuthResponse response = oauthAccountService.loginOrCreateUser(new OAuthUserProfile(
                AuthProvider.GITHUB,
                "42",
                email,
                "coderdev",
                "Coder Dev",
                "https://avatars.example.com/coder.png"
        ));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo(email);
        assertThat(response.user().provider()).isEqualTo("GITHUB");
    }

    @Test
    void rejectsConflictingProviderForExistingEmail() {
        String email = "local-" + System.nanoTime() + "@example.com";
        User existingUser = new User();
        existingUser.setUsername("localuser" + System.nanoTime());
        existingUser.setEmail(email);
        existingUser.setPasswordHash(passwordEncoder.encode("StrongPass123"));
        existingUser.setFullName("Local User");
        existingUser.setRole(Role.USER);
        existingUser.setProvider(AuthProvider.LOCAL);
        existingUser.setActive(true);
        userRepository.save(existingUser);

        assertThatThrownBy(() -> oauthAccountService.loginOrCreateUser(new OAuthUserProfile(
                AuthProvider.GOOGLE,
                "google-1",
                email,
                "googleuser",
                "Google User",
                null
        ))).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("different sign-in method");
    }
}
