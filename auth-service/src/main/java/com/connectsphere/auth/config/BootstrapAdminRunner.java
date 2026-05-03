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
        String username = resolveUsername(email);
        Optional<User> existingAdmin = userRepository.findByEmail(email);
        if (existingAdmin.isPresent()) {
            User admin = existingAdmin.get();
            admin.setRole(Role.ADMIN);
            admin.setActive(true);
            admin.setEmailVerified(true);
            admin.setProvider(AuthProvider.LOCAL);
            admin.setPasswordHash(passwordEncoder.encode(properties.password().trim()));
            admin.setFullName(defaultIfBlank(properties.fullName(), admin.getFullName()));
            if (!admin.getUsername().equals(username) && !userRepository.existsByUsername(username)) {
                admin.setUsername(username);
            }
            userRepository.save(admin);
            return;
        }

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

    private String resolveUsername(String email) {
        String configured = defaultIfBlank(properties.username(), "admin");
        Optional<User> existingUsernameOwner = userRepository.findByUsername(configured);
        if (existingUsernameOwner.isEmpty() || existingUsernameOwner.get().getEmail().equalsIgnoreCase(email)) {
            return configured;
        }

        String emailPrefix = email.split("@", 2)[0].replaceAll("[^A-Za-z0-9_]", "");
        String base = defaultIfBlank(emailPrefix, "admin");
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }
}
