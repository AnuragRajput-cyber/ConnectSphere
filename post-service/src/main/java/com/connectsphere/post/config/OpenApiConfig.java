package com.connectsphere.post.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI postServiceOpenAPI() {
        return new OpenAPI()
                // Keeping the document free of fixed server URLs makes it easier to reuse behind
                // a gateway, because Spring can honor forwarded headers from the upstream proxy.
                .info(new Info()
                        .title("ConnectSphere Post Service API")
                        .version("v1")
                        .description("""
                                OpenAPI definition for the post-management service.
                                The generated paths remain relative so the same Swagger setup works
                                both on the standalone service and when routed through an API gateway.
                                """)
                        .contact(new Contact().name("ConnectSphere Team")));
    }
}
