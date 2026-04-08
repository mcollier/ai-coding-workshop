package com.example.taskmanager.api.exception;

import com.example.taskmanager.application.services.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for REST API.
 * 
 * Uses Spring 6 @RestControllerAdvice and ProblemDetail (RFC 7807)
 * for consistent error responses across all endpoints.
 * 
 * Handles:
 * - TaskNotFoundException → 404 Not Found
 * - IllegalStateException → 400 Bad Request (business rule violations)
 * - IllegalArgumentException → 400 Bad Request (invalid input)
 * - MethodArgumentNotValidException → 400 Bad Request (validation errors)
 * - Generic exceptions → 500 Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle task not found exceptions.
     * Returns 404 Not Found with ProblemDetail.
     * 
     * @param ex the TaskNotFoundException
     * @return ProblemDetail with 404 status
     */
    @ExceptionHandler(TaskService.TaskNotFoundException.class)
    public ProblemDetail handleTaskNotFoundException(TaskService.TaskNotFoundException ex) {
        logger.debug("Task not found: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Task Not Found");
        problemDetail.setType(URI.create("https://api.taskmanager.example.com/problems/task-not-found"));
        problemDetail.setProperty("taskId", ex.getTaskId().value().toString());
        
        return problemDetail;
    }
    
    /**
     * Handle illegal state exceptions (business rule violations).
     * Returns 400 Bad Request with ProblemDetail.
     * 
     * Examples:
     * - Cannot start task that's not in PENDING status
     * - Cannot complete a cancelled task
     * - Cannot update a completed task
     * 
     * @param ex the IllegalStateException
     * @return ProblemDetail with 400 status
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex) {
        logger.debug("Illegal state: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Operation");
        problemDetail.setType(URI.create("https://api.taskmanager.example.com/problems/invalid-operation"));
        
        return problemDetail;
    }
    
    /**
     * Handle illegal argument exceptions (invalid input).
     * Returns 400 Bad Request with ProblemDetail.
     * 
     * Examples:
     * - Task title is blank
     * - Task title exceeds maximum length
     * 
     * @param ex the IllegalArgumentException
     * @return ProblemDetail with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.debug("Illegal argument: {}", ex.getMessage());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problemDetail.setTitle("Invalid Input");
        problemDetail.setType(URI.create("https://api.taskmanager.example.com/problems/invalid-input"));
        
        return problemDetail;
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     * Returns 400 Bad Request with ProblemDetail containing field errors.
     * 
     * Examples:
     * - @NotBlank validation failed on title
     * - @Size validation failed on title or description
     * 
     * @param ex the MethodArgumentNotValidException
     * @return ProblemDetail with 400 status and validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        logger.debug("Validation failed: {} field error(s)", ex.getFieldErrorCount());
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields"
        );
        problemDetail.setTitle("Validation Error");
        problemDetail.setType(URI.create("https://api.taskmanager.example.com/problems/validation-error"));
        
        // Add field-specific validation errors
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        problemDetail.setProperty("errors", fieldErrors);
        
        return problemDetail;
    }
    
    /**
     * Handle all other uncaught exceptions.
     * Returns 500 Internal Server Error with ProblemDetail.
     * Logs the full stack trace for debugging.
     * 
     * @param ex the Exception
     * @return ProblemDetail with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.taskmanager.example.com/problems/internal-error"));
        
        // In production, you might want to hide the exception details
        // For now, include them for debugging
        if (logger.isDebugEnabled()) {
            problemDetail.setProperty("exceptionType", ex.getClass().getName());
            problemDetail.setProperty("exceptionMessage", ex.getMessage());
        }
        
        return problemDetail;
    }
}
