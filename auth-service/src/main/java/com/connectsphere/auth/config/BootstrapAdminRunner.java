package com.connectsphere.auth.config;

import com.connectsphere.auth.entity.AuthProvider;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.repository.UserRepository;
import java.util.Optional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapAdminRunner implements CommandLineRunner {

    private final BootstrapAdminProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public BootstrapAdminRunner(
            BootstrapAdminProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.enabled()) {
            return;
        }
        if (properties.email() == null || properties.email().isBlank()) {
            return;
        }
        if (properties.password() == null || properties.password().isBlank()) {
            return;
        }

        String email = properties.email().trim().toLowerCase();
        String configuredUsername = defaultIfBlank(properties.username(), "admin");

        Optional<User> existingAdmin = userRepository.findByEmailIgnoreCase(email);
        if (existingAdmin.isPresent()) {
            updateAdmin(existingAdmin.get(), configuredUsername);
            return;
        }

        Optional<User> existingUsernameOwner = userRepository.findByUsernameIgnoreCase(configuredUsername);
        if (existingUsernameOwner.isPresent()) {
            updateAdmin(existingUsernameOwner.get(), configuredUsername);
            return;
        }

        String username = resolveAvailableUsername(configuredUsername, email);
        User admin = new User();
        admin.setEmail(email);
        admin.setUsername(username);
        admin.setFullName(defaultIfBlank(properties.fullName(), "ConnectSphere Admin"));
        admin.setPasswordHash(passwordEncoder.encode(properties.password().trim()));
        admin.setProvider(AuthProvider.LOCAL);
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setEmailVerified(true);
        admin.setPrivateAccount(false);

        userRepository.save(admin);
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String resolveAvailableUsername(String configured, String email) {
        if (!userRepository.existsByUsernameIgnoreCase(configured)) {
            return configured;
        }
        String emailPrefix = email.split("@", 2)[0].replaceAll("[^\\w]", "");
        String base = defaultIfBlank(emailPrefix, "admin");
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private void updateAdmin(User admin, String configuredUsername) {
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setEmailVerified(true);
        admin.setProvider(AuthProvider.LOCAL);
        admin.setPasswordHash(passwordEncoder.encode(properties.password().trim()));
        admin.setFullName(defaultIfBlank(properties.fullName(), admin.getFullName()));
        if (admin.getUsername() == null || admin.getUsername().isBlank()) {
            admin.setUsername(configuredUsername);
        }
        userRepository.save(admin);
    }
}
