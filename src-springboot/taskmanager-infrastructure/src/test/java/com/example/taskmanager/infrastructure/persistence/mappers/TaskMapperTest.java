package com.example.taskmanager.infrastructure.persistence.mappers;

import com.example.taskmanager.domain.tasks.Task;
import com.example.taskmanager.domain.tasks.TaskId;
import com.example.taskmanager.domain.tasks.TaskStatus;
import com.example.taskmanager.infrastructure.persistence.entities.TaskEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskMapper.
 * Verifies translation between domain Task and infrastructure TaskEntity.
 * 
 * These tests don't require a database since they only test the mapping logic.
 */
@DisplayName("TaskMapper Unit Tests")
class TaskMapperTest {

    private TaskMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TaskMapper();
    }

    @Nested
    @DisplayName("toEntity() - Domain to Infrastructure")
    class ToEntityTests {

        @Test
        @DisplayName("should convert domain Task to TaskEntity")
        void shouldConvertDomainTaskToEntity() {
            // Arrange
            Task task = Task.create("Test Task", "Test Description");

            // Act
            TaskEntity entity = mapper.toEntity(task);

            // Assert
            assertNotNull(entity);
            assertEquals(task.getId().value(), entity.getId());
            assertEquals(task.getTitle(), entity.getTitle());
            assertEquals(task.getDescription(), entity.getDescription());
            assertEquals(task.getStatus().name(), entity.getStatus());
            assertEquals(task.getCreatedAt(), entity.getCreatedAt());
            assertEquals(task.getCompletedAt(), entity.getCompletedAt());
        }

        @Test
        @DisplayName("should handle task with no description")
        void shouldHandleTaskWithNoDescription() {
            // Arrange
            Task task = Task.create("Task with no description");

            // Act
            TaskEntity entity = mapper.toEntity(task);

            // Assert
            assertNotNull(entity);
            assertNull(entity.getDescription());
        }

        @Test
        @DisplayName("should preserve all task statuses")
        void shouldPreserveAllTaskStatuses() {
            // Test PENDING
            Task pendingTask = Task.create("Pending");
            TaskEntity pendingEntity = mapper.toEntity(pendingTask);
            assertEquals("PENDING", pendingEntity.getStatus());

            // Test IN_PROGRESS
            Task inProgressTask = Task.create("In Progress");
            inProgressTask.start();
            TaskEntity inProgressEntity = mapper.toEntity(inProgressTask);
            assertEquals("IN_PROGRESS", inProgressEntity.getStatus());

            // Test COMPLETED
            Task completedTask = Task.create("Completed");
            completedTask.complete();
            TaskEntity completedEntity = mapper.toEntity(completedTask);
            assertEquals("COMPLETED", completedEntity.getStatus());

            // Test CANCELLED
            Task cancelledTask = Task.create("Cancelled");
            cancelledTask.cancel();
            TaskEntity cancelledEntity = mapper.toEntity(cancelledTask);
            assertEquals("CANCELLED", cancelledEntity.getStatus());
        }

        @Test
        @DisplayName("should preserve completed timestamp")
        void shouldPreserveCompletedTimestamp() {
            // Arrange
            Task task = Task.create("Completed Task");
            task.complete();

            // Act
            TaskEntity entity = mapper.toEntity(task);

            // Assert
            assertNotNull(entity.getCompletedAt());
            assertEquals(task.getCompletedAt(), entity.getCompletedAt());
        }

        @Test
        @DisplayName("should return null for null task")
        void shouldReturnNullForNullTask() {
            // Act
            TaskEntity entity = mapper.toEntity(null);

            // Assert
            assertNull(entity);
        }
    }

    @Nested
    @DisplayName("toDomain() - Infrastructure to Domain")
    class ToDomainTests {

        @Test
        @DisplayName("should convert TaskEntity to domain Task")
        void shouldConvertEntityToDomainTask() {
            // Arrange
            UUID id = UUID.randomUUID();
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            
            TaskEntity entity = new TaskEntity();
            entity.setId(id);
            entity.setTitle("Test Task");
            entity.setDescription("Test Description");
            entity.setStatus("PENDING");
            entity.setCreatedAt(createdAt);
            entity.setCompletedAt(null);

            // Act
            Task task = mapper.toDomain(entity);

            // Assert
            assertNotNull(task);
            assertEquals(id, task.getId().value());
            assertEquals("Test Task", task.getTitle());
            assertEquals("Test Description", task.getDescription());
            assertEquals(TaskStatus.PENDING, task.getStatus());
            assertNull(task.getCompletedAt());
        }

        @Test
        @DisplayName("should handle entity with no description")
        void shouldHandleEntityWithNoDescription() {
            // Arrange
            TaskEntity entity = new TaskEntity();
            entity.setId(UUID.randomUUID());
            entity.setTitle("Task");
            entity.setDescription(null);
            entity.setStatus("PENDING");
            entity.setCreatedAt(LocalDateTime.now());
            entity.setCompletedAt(null);

            // Act
            Task task = mapper.toDomain(entity);

            // Assert
            assertNotNull(task);
            assertNull(task.getDescription());
        }

        @Test
        @DisplayName("should convert all task statuses")
        void shouldConvertAllTaskStatuses() {
            // Test PENDING
            TaskEntity pendingEntity = createEntity(TaskStatus.PENDING);
            Task pendingTask = mapper.toDomain(pendingEntity);
            assertEquals(TaskStatus.PENDING, pendingTask.getStatus());

            // Test IN_PROGRESS
            TaskEntity inProgressEntity = createEntity(TaskStatus.IN_PROGRESS);
            Task inProgressTask = mapper.toDomain(inProgressEntity);
            assertEquals(TaskStatus.IN_PROGRESS, inProgressTask.getStatus());

            // Test COMPLETED
            TaskEntity completedEntity = createEntity(TaskStatus.COMPLETED);
            completedEntity.setCompletedAt(LocalDateTime.now());
            Task completedTask = mapper.toDomain(completedEntity);
            assertEquals(TaskStatus.COMPLETED, completedTask.getStatus());

            // Test CANCELLED
            TaskEntity cancelledEntity = createEntity(TaskStatus.CANCELLED);
            Task cancelledTask = mapper.toDomain(cancelledEntity);
            assertEquals(TaskStatus.CANCELLED, cancelledTask.getStatus());
        }

        @Test
        @DisplayName("should preserve completed timestamp")
        void shouldPreserveCompletedTimestamp() {
            // Arrange
            LocalDateTime completedAt = LocalDateTime.now();
            TaskEntity entity = createEntity(TaskStatus.COMPLETED);
            entity.setCompletedAt(completedAt);

            // Act
            Task task = mapper.toDomain(entity);

            // Assert
            assertNotNull(task.getCompletedAt());
        }

        @Test
        @DisplayName("should return null for null entity")
        void shouldReturnNullForNullEntity() {
            // Act
            Task task = mapper.toDomain(null);

            // Assert
            assertNull(task);
        }

        private TaskEntity createEntity(TaskStatus status) {
            TaskEntity entity = new TaskEntity();
            entity.setId(UUID.randomUUID());
            entity.setTitle("Test Task");
            entity.setDescription("Description");
            entity.setStatus(status.name());
            entity.setCreatedAt(LocalDateTime.now().minusDays(1));
            entity.setCompletedAt(null);
            return entity;
        }
    }

    @Nested
    @DisplayName("Round-trip conversion")
    class RoundTripTests {

        @Test
        @DisplayName("should preserve all data in round-trip conversion")
        void shouldPreserveAllDataInRoundTripConversion() {
            // Arrange
            Task originalTask = Task.create("Original Task", "Original Description");

            // Act - Convert to entity and back to domain
            TaskEntity entity = mapper.toEntity(originalTask);
            Task roundTripTask = mapper.toDomain(entity);

            // Assert
            assertEquals(originalTask.getId().value(), roundTripTask.getId().value());
            assertEquals(originalTask.getTitle(), roundTripTask.getTitle());
            assertEquals(originalTask.getDescription(), roundTripTask.getDescription());
            assertEquals(originalTask.getStatus(), roundTripTask.getStatus());
        }

        @Test
        @DisplayName("should preserve completed task state in round-trip")
        void shouldPreserveCompletedTaskStateInRoundTrip() {
            // Arrange
            Task originalTask = Task.create("Completed Task");
            originalTask.complete();

            // Act
            TaskEntity entity = mapper.toEntity(originalTask);
            Task roundTripTask = mapper.toDomain(entity);

            // Assert
            assertEquals(TaskStatus.COMPLETED, roundTripTask.getStatus());
            assertNotNull(roundTripTask.getCompletedAt());
        }
    }
}
