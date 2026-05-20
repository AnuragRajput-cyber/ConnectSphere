package com.connectsphere.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.bootstrap.admin")
public record BootstrapAdminProperties(
        boolean enabled,
        String email,
        String username,
        String fullName,
        String password
){
}

