package com.connectsphere.chat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI chatOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ConnectSphere Chat Service API")
                        .version("v1")
                        .description("Conversation history plus WebSocket chat messaging and typing indicators.")
                        .contact(new Contact().name("ConnectSphere Team")));
    }
}
