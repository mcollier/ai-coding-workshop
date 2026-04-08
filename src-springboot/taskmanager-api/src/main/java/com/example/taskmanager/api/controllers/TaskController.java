package com.example.taskmanager.api.controllers;

import com.example.taskmanager.api.dto.CreateTaskRequest;
import com.example.taskmanager.api.dto.TaskResponse;
import com.example.taskmanager.api.dto.UpdateTaskRequest;
import com.example.taskmanager.application.services.TaskService;
import com.example.taskmanager.domain.tasks.Task;
import com.example.taskmanager.domain.tasks.TaskId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for task management operations.
 * 
 * Follows Spring Boot REST conventions:
 * - Thin controller - delegates immediately to application service
 * - Uses DTOs for all requests and responses (never exposes domain entities)
 * - Returns ResponseEntity for explicit HTTP status codes
 * - Uses @Valid for request body validation
 * - Follows RESTful resource naming (/api/tasks)
 * 
 * Endpoints:
 * - POST   /api/tasks              - Create new task
 * - GET    /api/tasks/{id}         - Get task by ID
 * - GET    /api/tasks              - List all tasks
 * - PATCH  /api/tasks/{id}         - Update task (partial)
 * - PUT    /api/tasks/{id}/start   - Start task
 * - PUT    /api/tasks/{id}/complete - Complete task
 * - PUT    /api/tasks/{id}/cancel  - Cancel task
 * - DELETE /api/tasks/{id}         - Delete task
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management API endpoints")
public class TaskController {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    
    private final TaskService taskService;
    
    /**
     * Create a new task.
     * 
     * @param request the task creation request (validated)
     * @return 201 Created with task response
     */
    @PostMapping
    @Operation(
        summary = "Create a new task",
        description = "Creates a new task with the provided title and optional description. The task starts in PENDING status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Task created successfully",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input - validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        logger.debug("Creating new task with title: {}", request.title());
        
        Task task = taskService.createTask(request.title(), request.description());
        TaskResponse response = TaskResponse.from(task);
        
        logger.info("Created task with ID: {}", task.getId().value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get a task by ID.
     * 
     * @param id the task ID
     * @return 200 OK with task response, or 404 Not Found
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get task by ID",
        description = "Retrieves a task by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Task found",
            content = @Content(schema = @Schema(implementation = TaskResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Task not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    public ResponseEntity<TaskResponse> getTask(
            @Parameter(description = "Task ID", required = true)
            @PathVariable UUID id) {
        logger.debug("Fetching task with ID: {}", id);
        
        return taskService.findTask(new TaskId(id))
                .map(TaskResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.debug("Task not found: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }
    
    /**
     * List all tasks.
     * 
     * @return 200 OK with list of task responses
     */
    @GetMapping
    @Operation(
        summary = "List all tasks",
        description = "Retrieves all tasks in the system"
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of tasks (may be empty)",
        content = @Content(schema = @Schema(implementation = TaskResponse.class))
    )
    public ResponseEntity<List<TaskResponse>> listTasks() {
        logger.debug("Listing all tasks");
        
        List<Task> tasks = taskService.findAllTasks();
        List<TaskResponse> responses = tasks.stream()
                .map(TaskResponse::from)
                .toList();
        
        logger.debug("Found {} tasks", responses.size());
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Update a task (partial update).
     * Updates only the fields provided in the request.
     * 
     * @param id the task ID
     * @param request the update request (validated)
     * @return 200 OK with updated task response, or 404 Not Found
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        logger.debug("Updating task {}: title={}, description={}", 
                id, request.title(), request.description());
        
        TaskId taskId = new TaskId(id);
        
        // Partial update: only update provided fields
        Task updated = null;
        if (request.title() != null && !request.title().isBlank()) {
            updated = taskService.updateTaskTitle(taskId, request.title());
        }
        if (request.description() != null) {
            // If we already updated title, fetch the latest version
            updated = taskService.updateTaskDescription(taskId, request.description());
        }
        
        // If nothing was updated, fetch current task
        if (updated == null) {
            updated = taskService.findTask(taskId)
                    .orElseThrow(() -> new TaskService.TaskNotFoundException(taskId));
        }
        
        TaskResponse response = TaskResponse.from(updated);
        logger.info("Updated task: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Start a task (change status to IN_PROGRESS).
     * 
     * @param id the task ID
     * @return 200 OK with updated task response, or 404 Not Found
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<TaskResponse> startTask(@PathVariable UUID id) {
        logger.debug("Starting task: {}", id);
        
        Task started = taskService.startTask(new TaskId(id));
        TaskResponse response = TaskResponse.from(started);
        
        logger.info("Started task: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Complete a task (change status to COMPLETED).
     * 
     * @param id the task ID
     * @return 200 OK with updated task response, or 404 Not Found
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> completeTask(@PathVariable UUID id) {
        logger.debug("Completing task: {}", id);
        
        Task completed = taskService.completeTask(new TaskId(id));
        TaskResponse response = TaskResponse.from(completed);
        
        logger.info("Completed task: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel a task (change status to CANCELLED).
     * 
     * @param id the task ID
     * @return 200 OK with updated task response, or 404 Not Found
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<TaskResponse> cancelTask(@PathVariable UUID id) {
        logger.debug("Cancelling task: {}", id);
        
        Task cancelled = taskService.cancelTask(new TaskId(id));
        TaskResponse response = TaskResponse.from(cancelled);
        
        logger.info("Cancelled task: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a task.
     * 
     * @param id the task ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        logger.debug("Deleting task: {}", id);
        
        taskService.deleteTask(new TaskId(id));
        
        logger.info("Deleted task: {}", id);
        return ResponseEntity.noContent().build();
    }
}
