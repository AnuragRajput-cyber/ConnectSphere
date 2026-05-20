package com.connectsphere.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI notificationOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("/")
                        .description("Current origin")))
                .info(new Info()
                        .title("ConnectSphere Notification Service API")
                        .version("v1")
                        .description("In-app notifications, unread counts, bulk dispatch, and email-alert stubs.")
                        .contact(new Contact().name("ConnectSphere Team")));
    }
}
