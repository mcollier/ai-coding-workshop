package com.example.taskmanager.domain.tasks;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Task aggregate.
 * Defines the contract for persistence operations using domain language.
 * 
 * This is a port (interface) in Clean Architecture - defined in the domain layer
 * but implemented in the infrastructure layer.
 * 
 * Domain-Driven Design principles:
 * - Use business-intent method names (not generic CRUD)
 * - Work with aggregate roots only
 * - Return domain objects, not data transfer objects
 */
public interface TaskRepository {
    
    /**
     * Save a task (create or update).
     * 
     * @param task the task to save
     * @return the saved task
     */
    Task save(Task task);
    
    /**
     * Find a task by its identifier.
     * 
     * @param id the task identifier
     * @return an Optional containing the task if found, or empty if not found
     */
    Optional<Task> findById(TaskId id);
    
    /**
     * Find all tasks with a specific status.
     * 
     * @param status the task status to filter by
     * @return a list of tasks matching the status
     */
    List<Task> findByStatus(TaskStatus status);
    
    /**
     * Find all tasks.
     * 
     * @return a list of all tasks
     */
    List<Task> findAll();
    
    /**
     * Find all pending tasks.
     * Convenience method using business language.
     * 
     * @return a list of pending tasks
     */
    default List<Task> findPendingTasks() {
        return findByStatus(TaskStatus.PENDING);
    }
    
    /**
     * Find all in-progress tasks.
     * Convenience method using business language.
     * 
     * @return a list of in-progress tasks
     */
    default List<Task> findInProgressTasks() {
        return findByStatus(TaskStatus.IN_PROGRESS);
    }
    
    /**
     * Find all completed tasks.
     * Convenience method using business language.
     * 
     * @return a list of completed tasks
     */
    default List<Task> findCompletedTasks() {
        return findByStatus(TaskStatus.COMPLETED);
    }
    
    /**
     * Delete a task by its identifier.
     * 
     * @param id the task identifier
     */
    void deleteById(TaskId id);
    
    /**
     * Check if a task exists by its identifier.
     * 
     * @param id the task identifier
     * @return true if the task exists, false otherwise
     */
    boolean existsById(TaskId id);
    
    /**
     * Count all tasks.
     * 
     * @return the total number of tasks
     */
    long count();
    
    /**
     * Count tasks by status.
     * 
     * @param status the task status
     * @return the number of tasks with the given status
     */
    long countByStatus(TaskStatus status);
}
