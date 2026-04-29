package com.connectsphere.search.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI searchOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ConnectSphere Search Service API")
                        .version("v1")
                        .description("Hashtag indexing, trending lookups, and delegated post/user search.")
                        .contact(new Contact().name("ConnectSphere Team")));
    }
}
