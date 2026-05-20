package com.connectsphere.follow.config;

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
    OpenAPI followServiceOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("/")
                        .description("Current origin")))
                .info(new Info()
                        .title("ConnectSphere Follow Service API")
                        .version("v1")
                        .description("Directed follow graph, counts, mutual follows, and user suggestions.")
                        .contact(new Contact().name("ConnectSphere Team")));
    }
}
