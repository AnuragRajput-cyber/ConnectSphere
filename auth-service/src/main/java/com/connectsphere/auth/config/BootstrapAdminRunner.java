package com.connectsphere.auth.config;

import com.connectsphere.auth.entity.AuthProvider;
import com.connectsphere.auth.entity.Role;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
        if (userRepository.findByEmail(properties.email().trim().toLowerCase()).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setEmail(properties.email().trim().toLowerCase());
        admin.setUsername(defaultIfBlank(properties.username(), "admin"));
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
}
