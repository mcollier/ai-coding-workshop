package com.example.taskmanager.api.dto;

import com.example.taskmanager.domain.tasks.Task;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for task data.
 * 
 * Uses Java record for immutability. This DTO represents the external
 * API contract and maps from the domain Task entity. Never expose domain
 * objects directly in REST responses.
 * 
 * @param id the task ID
 * @param title the task title
 * @param description the task description (nullable)
 * @param status the task status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
 * @param createdAt when the task was created
 * @param completedAt when the task was completed (nullable)
 */
public record TaskResponse(
    UUID id,
    String title,
    String description,
    String status,
    LocalDateTime createdAt,
    LocalDateTime completedAt
) {
    /**
     * Factory method to create a TaskResponse from a domain Task.
     * Maps domain object to DTO, ensuring proper encapsulation and
     * preventing domain entities from leaking into the API layer.
     * 
     * @param task the domain task
     * @return a TaskResponse DTO
     */
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId().value(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus().name(),
            task.getCreatedAt(),
            task.getCompletedAt()
        );
    }
}
