package com.example.taskmanager.infrastructure.persistence.repositories;

import com.example.taskmanager.domain.tasks.Task;
import com.example.taskmanager.domain.tasks.TaskId;
import com.example.taskmanager.domain.tasks.TaskRepository;
import com.example.taskmanager.domain.tasks.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JpaTaskRepositoryAdapter.
 * Uses H2 in-memory database with PostgreSQL compatibility mode.
 * 
 * Tests verify:
 * - Database persistence
 * - Mapper translation (domain <-> entity)
 * - Spring Data JPA integration
 * - Repository contract implementation
 * 
 * NOTE: For production-like testing with real PostgreSQL using Testcontainers,
 * see JpaTaskRepositoryAdapterTestcontainersTest (requires Docker).
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
@DisplayName("JPA Task Repository Integration Tests")
class JpaTaskRepositoryAdapterIntegrationTest {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Test
    @DisplayName("should save and retrieve task by ID")
    void shouldSaveAndRetrieveTaskById() {
        // Arrange
        Task task = Task.create("Test Task", "Test Description");
        TaskId taskId = task.getId();
        
        // Act
        Task saved = taskRepository.save(task);
        Optional<Task> retrieved = taskRepository.findById(taskId);
        
        // Assert
        assertNotNull(saved);
        assertTrue(retrieved.isPresent());
        assertEquals(taskId, retrieved.get().getId());
        assertEquals("Test Task", retrieved.get().getTitle());
        assertEquals("Test Description", retrieved.get().getDescription());
        assertEquals(TaskStatus.PENDING, retrieved.get().getStatus());
    }
    
    @Test
    @DisplayName("should return empty when task not found")
    void shouldReturnEmptyWhenTaskNotFound() {
        // Arrange
        TaskId nonExistentId = TaskId.newId();
        
        // Act
        Optional<Task> result = taskRepository.findById(nonExistentId);
        
        // Assert
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("should update existing task")
    void shouldUpdateExistingTask() {
        // Arrange
        Task task = Task.create("Original Title");
        Task saved = taskRepository.save(task);
        TaskId taskId = saved.getId();
        
        // Act
        saved.updateTitle("Updated Title");
        taskRepository.save(saved);
        Optional<Task> retrieved = taskRepository.findById(taskId);
        
        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals("Updated Title", retrieved.get().getTitle());
    }
    
    @Test
    @DisplayName("should find all tasks")
    void shouldFindAllTasks() {
        // Arrange
        Task task1 = Task.create("Task 1");
        Task task2 = Task.create("Task 2");
        Task task3 = Task.create("Task 3");
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        // Act
        List<Task> allTasks = taskRepository.findAll();
        
        // Assert
        assertTrue(allTasks.size() >= 3, "Should have at least 3 tasks");
    }
    
    @Test
    @DisplayName("should find tasks by status")
    void shouldFindTasksByStatus() {
        // Arrange
        Task pendingTask1 = Task.create("Pending 1");
        Task pendingTask2 = Task.create("Pending 2");
        Task inProgressTask = Task.create("In Progress");
        inProgressTask.start();
        
        taskRepository.save(pendingTask1);
        taskRepository.save(pendingTask2);
        taskRepository.save(inProgressTask);
        
        // Act
        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);
        
        // Assert
        assertTrue(pendingTasks.size() >= 2, "Should have at least 2 pending tasks");
        assertTrue(inProgressTasks.size() >= 1, "Should have at least 1 in-progress task");
        
        // Verify all pending tasks have correct status
        for (Task task : pendingTasks) {
            assertEquals(TaskStatus.PENDING, task.getStatus());
        }
        
        // Verify all in-progress tasks have correct status
        for (Task task : inProgressTasks) {
            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        }
    }
    
    @Test
    @DisplayName("should delete task by ID")
    void shouldDeleteTaskById() {
        // Arrange
        Task task = Task.create("Task to Delete");
        Task saved = taskRepository.save(task);
        TaskId taskId = saved.getId();
        
        // Verify task exists
        assertTrue(taskRepository.existsById(taskId));
        
        // Act
        taskRepository.deleteById(taskId);
        
        // Assert
        assertFalse(taskRepository.existsById(taskId));
        Optional<Task> deleted = taskRepository.findById(taskId);
        assertFalse(deleted.isPresent());
    }
    
    @Test
    @DisplayName("should check if task exists")
    void shouldCheckIfTaskExists() {
        // Arrange
        Task task = Task.create("Existing Task");
        Task saved = taskRepository.save(task);
        TaskId existingId = saved.getId();
        TaskId nonExistentId = TaskId.newId();
        
        // Act & Assert
        assertTrue(taskRepository.existsById(existingId));
        assertFalse(taskRepository.existsById(nonExistentId));
    }
    
    @Test
    @DisplayName("should count all tasks")
    void shouldCountAllTasks() {
        // Arrange
        long initialCount = taskRepository.count();
        
        Task task1 = Task.create("Count Task 1");
        Task task2 = Task.create("Count Task 2");
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        // Act
        long finalCount = taskRepository.count();
        
        // Assert
        assertEquals(initialCount + 2, finalCount);
    }
    
    @Test
    @DisplayName("should count tasks by status")
    void shouldCountTasksByStatus() {
        // Arrange
        long initialPendingCount = taskRepository.countByStatus(TaskStatus.PENDING);
        long initialCompletedCount = taskRepository.countByStatus(TaskStatus.COMPLETED);
        
        Task pendingTask = Task.create("Pending");
        Task completedTask = Task.create("Completed");
        completedTask.complete();
        
        taskRepository.save(pendingTask);
        taskRepository.save(completedTask);
        
        // Act
        long finalPendingCount = taskRepository.countByStatus(TaskStatus.PENDING);
        long finalCompletedCount = taskRepository.countByStatus(TaskStatus.COMPLETED);
        
        // Assert
        assertEquals(initialPendingCount + 1, finalPendingCount);
        assertEquals(initialCompletedCount + 1, finalCompletedCount);
    }
    
    @Test
    @DisplayName("should persist task state transitions")
    void shouldPersistTaskStateTransitions() {
        // Arrange
        Task task = Task.create("State Transition Task");
        Task saved = taskRepository.save(task);
        TaskId taskId = saved.getId();
        
        // Act - Start task
        saved.start();
        taskRepository.save(saved);
        Optional<Task> started = taskRepository.findById(taskId);
        
        // Assert - Task is in progress
        assertTrue(started.isPresent());
        assertEquals(TaskStatus.IN_PROGRESS, started.get().getStatus());
        
        // Act - Complete task
        started.get().complete();
        taskRepository.save(started.get());
        Optional<Task> completed = taskRepository.findById(taskId);
        
        // Assert - Task is completed
        assertTrue(completed.isPresent());
        assertEquals(TaskStatus.COMPLETED, completed.get().getStatus());
        assertNotNull(completed.get().getCompletedAt());
    }
    
    @Test
    @DisplayName("should preserve timestamps when persisting")
    void shouldPreserveTimestampsWhenPersisting() {
        // Arrange
        Task task = Task.create("Timestamp Task");
        
        // Act
        Task saved = taskRepository.save(task);
        TaskId taskId = saved.getId();
        Optional<Task> retrieved = taskRepository.findById(taskId);
        
        // Assert
        assertTrue(retrieved.isPresent());
        assertNotNull(retrieved.get().getCreatedAt());
        // Note: Database may truncate timestamp precision, so we check they're within 1 second
        assertTrue(
            Math.abs(java.time.Duration.between(saved.getCreatedAt(), retrieved.get().getCreatedAt()).toMillis()) < 1000,
            "Timestamps should be within 1 second of each other"
        );
        assertNull(retrieved.get().getCompletedAt()); // Not completed yet
    }
    
    @Test
    @DisplayName("should handle null description")
    void shouldHandleNullDescription() {
        // Arrange
        Task task = Task.create("Task with no description");
        
        // Act
        Task saved = taskRepository.save(task);
        Optional<Task> retrieved = taskRepository.findById(saved.getId());
        
        // Assert
        assertTrue(retrieved.isPresent());
        assertNull(retrieved.get().getDescription());
    }
    
    @Test
    @DisplayName("should use convenience methods from repository interface")
    void shouldUseConvenienceMethodsFromRepositoryInterface() {
        // Arrange
        Task pendingTask = Task.create("Pending");
        Task inProgressTask = Task.create("In Progress");
        inProgressTask.start();
        Task completedTask = Task.create("Completed");
        completedTask.complete();
        
        taskRepository.save(pendingTask);
        taskRepository.save(inProgressTask);
        taskRepository.save(completedTask);
        
        // Act - Use default methods from TaskRepository interface
        List<Task> pending = taskRepository.findPendingTasks();
        List<Task> inProgress = taskRepository.findInProgressTasks();
        List<Task> completed = taskRepository.findCompletedTasks();
        
        // Assert
        assertTrue(pending.size() >= 1);
        assertTrue(inProgress.size() >= 1);
        assertTrue(completed.size() >= 1);
        
        // Verify correct statuses
        pending.forEach(t -> assertEquals(TaskStatus.PENDING, t.getStatus()));
        inProgress.forEach(t -> assertEquals(TaskStatus.IN_PROGRESS, t.getStatus()));
        completed.forEach(t -> assertEquals(TaskStatus.COMPLETED, t.getStatus()));
    }
}
