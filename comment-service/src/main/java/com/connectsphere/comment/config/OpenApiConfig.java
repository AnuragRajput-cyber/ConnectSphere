package com.connectsphere.comment.config;

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
    OpenAPI commentServiceOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("/")
                        .description("Current origin")))
                .info(new Info()
                        .title("ConnectSphere Comment Service API")
                        .version("v1")
                        .description("Threaded comments, replies, comment likes, and post-level comment counts.")
                        .contact(new Contact().name("ConnectSphere Team")));
    }
}
