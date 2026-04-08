package com.example.taskmanager.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new task.
 * 
 * Uses Java record for immutability and validation annotations.
 * This DTO serves as the external API contract and should never expose
 * internal domain objects directly.
 * 
 * @param title the task title (required, max 200 characters)
 * @param description the task description (optional)
 */
public record CreateTaskRequest(
    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Task title cannot exceed 200 characters")
    String title,
    
    @Size(max = 2000, message = "Task description cannot exceed 2000 characters")
    String description
) {
    /**
     * Factory method for creating a request with only a title.
     * 
     * @param title the task title
     * @return a CreateTaskRequest instance
     */
    public static CreateTaskRequest of(String title) {
        return new CreateTaskRequest(title, null);
    }
    
    /**
     * Factory method for creating a request with title and description.
     * 
     * @param title the task title
     * @param description the task description
     * @return a CreateTaskRequest instance
     */
    public static CreateTaskRequest of(String title, String description) {
        return new CreateTaskRequest(title, description);
    }
}
