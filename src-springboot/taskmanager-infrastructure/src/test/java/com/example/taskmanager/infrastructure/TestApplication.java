package com.example.taskmanager.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot test application for infrastructure module.
 * Used by integration tests to bootstrap Spring context.
 * 
 * This is a minimal configuration that enables:
 * - JPA entity scanning
 * - Spring Data JPA repositories
 * - Component scanning for infrastructure beans
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.taskmanager.infrastructure.persistence.repositories")
@EntityScan(basePackages = "com.example.taskmanager.infrastructure.persistence.entities")
public class TestApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
