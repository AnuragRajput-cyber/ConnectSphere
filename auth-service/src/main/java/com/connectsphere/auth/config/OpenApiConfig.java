package com.connectsphere.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                // We avoid hard-coded server URLs so Swagger keeps working when the service is accessed
                // directly today and later through an API gateway with forwarded headers.
                .info(new Info()
                        .title("ConnectSphere Auth Service API")
                        .version("v1")
                        .description("""
                                OpenAPI definition for the authentication and user-management service.
                                The routes stay relative so the same specification can be consumed
                                directly on the service or later through a single API gateway.
                                """)
                        .contact(new Contact().name("ConnectSphere Team")))
                .servers(List.of(new Server()
                        .url("/")
                        .description("Current origin")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT access token here as a Bearer token.")));
    }
}
