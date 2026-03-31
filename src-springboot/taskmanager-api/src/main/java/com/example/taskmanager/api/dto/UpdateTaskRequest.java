package com.example.taskmanager.api.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing task.
 * 
 * Uses Java record for immutability. All fields are optional to support
 * partial updates (PATCH semantics). Validation annotations ensure data
 * integrity when fields are provided.
 * 
 * @param title the new task title (optional, max 200 characters)
 * @param description the new task description (optional, max 2000 characters)
 */
public record UpdateTaskRequest(
    @Size(max = 200, message = "Task title cannot exceed 200 characters")
    String title,
    
    @Size(max = 2000, message = "Task description cannot exceed 2000 characters")
    String description
) {
    /**
     * Check if title should be updated.
     * 
     * @return true if title is not null
     */
    public boolean hasTitle() {
        return title != null;
    }
    
    /**
     * Check if description should be updated.
     * 
     * @return true if description field is present (even if null value)
     */
    public boolean hasDescription() {
        return true; // In record, we can't distinguish between null and absent
    }
}
