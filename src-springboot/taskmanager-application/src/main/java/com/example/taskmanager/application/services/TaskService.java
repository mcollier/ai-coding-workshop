package com.example.taskmanager.application.services;

import com.example.taskmanager.domain.tasks.Task;
import com.example.taskmanager.domain.tasks.TaskId;
import com.example.taskmanager.domain.tasks.TaskRepository;
import com.example.taskmanager.domain.tasks.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Application service for task management use cases.
 * Orchestrates domain logic and repository operations.
 * 
 * This is the application layer - it coordinates domain objects
 * but contains no business logic (that's in the domain layer).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public final class TaskService {
    
    private final TaskRepository taskRepository;
    
    /**
     * Create a new task with a title.
     * 
     * @param title the task title
     * @return the created task
     * @throws IllegalArgumentException if title is invalid
     */
    @Transactional
    public Task createTask(String title) {
        Task task = Task.create(title);
        return taskRepository.save(task);
    }
    
    /**
     * Create a new task with title and description.
     * 
     * @param title the task title
     * @param description the task description
     * @return the created task
     * @throws IllegalArgumentException if title is invalid
     */
    @Transactional
    public Task createTask(String title, String description) {
        Task task = Task.create(title, description);
        return taskRepository.save(task);
    }
    
    /**
     * Find a task by ID.
     * 
     * @param id the task ID
     * @return the task if found
     */
    public Optional<Task> findTask(TaskId id) {
        return taskRepository.findById(id);
    }
    
    /**
     * Find all tasks.
     * 
     * @return list of all tasks
     */
    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }
    
    /**
     * Find tasks by status.
     * 
     * @param status the task status
     * @return list of tasks with the given status
     */
    public List<Task> findTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
    
    /**
     * Start a task (transition from PENDING to IN_PROGRESS).
     * 
     * @param id the task ID
     * @return the updated task
     * @throws IllegalStateException if task cannot be started
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public Task startTask(TaskId id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        task.start();
        return taskRepository.save(task);
    }
    
    /**
     * Complete a task.
     * 
     * @param id the task ID
     * @return the updated task
     * @throws IllegalStateException if task cannot be completed
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public Task completeTask(TaskId id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        task.complete();
        return taskRepository.save(task);
    }
    
    /**
     * Cancel a task.
     * 
     * @param id the task ID
     * @return the updated task
     * @throws IllegalStateException if task cannot be cancelled
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public Task cancelTask(TaskId id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        task.cancel();
        return taskRepository.save(task);
    }
    
    /**
     * Update task title.
     * 
     * @param id the task ID
     * @param newTitle the new title
     * @return the updated task
     * @throws IllegalArgumentException if title is invalid
     * @throws IllegalStateException if task cannot be updated
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public Task updateTaskTitle(TaskId id, String newTitle) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        task.updateTitle(newTitle);
        return taskRepository.save(task);
    }
    
    /**
     * Update task description.
     * 
     * @param id the task ID
     * @param newDescription the new description
     * @return the updated task
     * @throws IllegalStateException if task cannot be updated
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public Task updateTaskDescription(TaskId id, String newDescription) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
        task.updateDescription(newDescription);
        return taskRepository.save(task);
    }
    
    /**
     * Delete a task.
     * 
     * @param id the task ID
     * @throws TaskNotFoundException if task not found
     */
    @Transactional
    public void deleteTask(TaskId id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }
    
    /**
     * Check if a task exists.
     * 
     * @param id the task ID
     * @return true if task exists
     */
    public boolean taskExists(TaskId id) {
        return taskRepository.existsById(id);
    }
    
    /**
     * Exception thrown when a task is not found.
     */
    public static class TaskNotFoundException extends RuntimeException {
        private final TaskId taskId;
        
        public TaskNotFoundException(TaskId taskId) {
            super("Task not found with ID: " + taskId.value());
            this.taskId = taskId;
        }
        
        public TaskId getTaskId() {
            return taskId;
        }
    }
}
