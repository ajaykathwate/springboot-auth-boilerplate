package com.example.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Spring Boot Auth Boilerplate API",
        version = "1.0",
        description = "Authentication API with JWT tokens, Google OAuth2, and token refresh capabilities",
        contact = @Contact(name = "API Support")
    ),
    servers = {
        @Server(url = "/", description = "Default Server")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "JWT access token for API authorization. Obtain token via /api/auth/google/login endpoint."
)
public class OpenApiConfig {
}
