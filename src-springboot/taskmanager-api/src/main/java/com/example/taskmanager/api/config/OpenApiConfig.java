package com.example.taskmanager.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for Task Manager API.
 * 
 * Provides interactive API documentation accessible at:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 * 
 * Uses Springdoc OpenAPI v2.x for Spring Boot 3.x compatibility.
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI taskManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API")
                        .description("""
                                RESTful API for managing tasks following Clean Architecture principles.
                                
                                Features:
                                - Create, read, update, and delete tasks
                                - Task state management (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
                                - Input validation and error handling
                                - RFC 7807 Problem Details for error responses
                                
                                Architecture:
                                - Domain-Driven Design with aggregates and value objects
                                - Hexagonal Architecture (Ports & Adapters)
                                - Spring Boot 3.x with Java 21
                                - PostgreSQL database with Spring Data JPA
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Task Manager Team")
                                .email("taskmanager@example.com")
                                .url("https://github.com/example/taskmanager"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.taskmanager.example.com")
                                .description("Production server")
                ));
    }
}
