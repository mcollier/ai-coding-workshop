package com.example.taskmanager.domain.tasks;

/**
 * Task status enumeration.
 * Represents the lifecycle states of a task.
 */
public enum TaskStatus {
    /**
     * Task has been created but not yet started.
     */
    PENDING,
    
    /**
     * Task is currently being worked on.
     */
    IN_PROGRESS,
    
    /**
     * Task has been completed successfully.
     */
    COMPLETED,
    
    /**
     * Task has been cancelled and will not be completed.
     */
    CANCELLED
}
