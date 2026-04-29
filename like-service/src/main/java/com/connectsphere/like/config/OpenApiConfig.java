package com.connectsphere.like.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI likeServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ConnectSphere Like Service API")
                        .version("v1")
                        .description("Polymorphic post and comment reactions with one reaction per user and target.")
                        .contact(new Contact().name("ConnectSphere Team")));
    }
}
