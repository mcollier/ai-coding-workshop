package com.example.taskmanager.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot Application entry point for Task Manager API.
 * 
 * Configures:
 * - Component scanning for all taskmanager packages
 * - JPA repositories from infrastructure layer
 * - JPA entities from infrastructure layer
 * - Auto-configuration for web, JPA, validation
 */
@SpringBootApplication(scanBasePackages = "com.example.taskmanager")
@EnableJpaRepositories(basePackages = "com.example.taskmanager.infrastructure.persistence.repositories")
@EntityScan(basePackages = "com.example.taskmanager.infrastructure.persistence.entities")
public class TaskManagerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
