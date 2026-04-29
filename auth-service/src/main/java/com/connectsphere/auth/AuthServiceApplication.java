package com.connectsphere.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.connectsphere.auth.config.JwtProperties;
import com.connectsphere.auth.config.BootstrapAdminProperties;
import com.connectsphere.auth.config.OtpProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, OtpProperties.class, BootstrapAdminProperties.class})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
