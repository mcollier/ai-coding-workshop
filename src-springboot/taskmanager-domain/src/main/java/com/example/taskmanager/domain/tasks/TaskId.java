package com.example.taskmanager.domain.tasks;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for Task aggregate.
 * Implements value object pattern - immutable and compared by value.
 */
public record TaskId(UUID value) {
    
    public TaskId {
        Objects.requireNonNull(value, "TaskId value cannot be null");
    }
    
    /**
     * Factory method to create a new TaskId with a random UUID.
     * 
     * @return a new TaskId instance
     */
    public static TaskId newId() {
        return new TaskId(UUID.randomUUID());
    }
    
    /**
     * Factory method to create a TaskId from an existing UUID.
     * 
     * @param value the UUID value
     * @return a TaskId instance
     */
    public static TaskId of(UUID value) {
        return new TaskId(value);
    }
    
    /**
     * Factory method to create a TaskId from a string UUID.
     * 
     * @param value the string representation of a UUID
     * @return a TaskId instance
     * @throws IllegalArgumentException if the string is not a valid UUID
     */
    public static TaskId of(String value) {
        Objects.requireNonNull(value, "TaskId string value cannot be null");
        return new TaskId(UUID.fromString(value));
    }
}
