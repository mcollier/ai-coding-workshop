package com.example.taskmanager.domain.tasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Task aggregate root.
 * Tests factory methods, business logic, invariants, and state transitions.
 */
@DisplayName("Task Aggregate Root")
class TaskTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("should create task with title and description")
        void shouldCreateTaskWithTitleAndDescription() {
            // Arrange
            String title = "Complete project documentation";
            String description = "Write comprehensive API docs";

            // Act
            Task task = Task.create(title, description);

            // Assert
            assertNotNull(task.getId(), "Task ID should be generated");
            assertEquals(title, task.getTitle(), "Title should match");
            assertEquals(description, task.getDescription(), "Description should match");
            assertEquals(TaskStatus.PENDING, task.getStatus(), "New task should be PENDING");
            assertNotNull(task.getCreatedAt(), "Created timestamp should be set");
            assertNull(task.getCompletedAt(), "Completed timestamp should be null");
        }

        @Test
        @DisplayName("should create task with title only")
        void shouldCreateTaskWithTitleOnly() {
            // Arrange
            String title = "Quick task";

            // Act
            Task task = Task.create(title);

            // Assert
            assertNotNull(task.getId());
            assertEquals(title, task.getTitle());
            assertNull(task.getDescription(), "Description should be null when not provided");
            assertEquals(TaskStatus.PENDING, task.getStatus());
        }

        @Test
        @DisplayName("should reconstitute task from persistence")
        void shouldReconstituteTaskFromPersistence() {
            // Arrange
            TaskId id = TaskId.newId();
            String title = "Existing task";
            String description = "From database";
            TaskStatus status = TaskStatus.IN_PROGRESS;
            LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
            LocalDateTime completedAt = null;

            // Act
            Task task = Task.reconstitute(id, title, description, status, createdAt, completedAt);

            // Assert
            assertEquals(id, task.getId());
            assertEquals(title, task.getTitle());
            assertEquals(description, task.getDescription());
            assertEquals(status, task.getStatus());
            assertNull(task.getCompletedAt());
        }

        @Test
        @DisplayName("should throw exception when title is null")
        void shouldThrowExceptionWhenTitleIsNull() {
            // Act & Assert
            NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> Task.create(null, "description")
            );
            assertTrue(exception.getMessage().contains("title"), "Error message should mention title");
        }

        @Test
        @DisplayName("should throw exception when title is blank")
        void shouldThrowExceptionWhenTitleIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> Task.create("", "description"));
            assertThrows(IllegalArgumentException.class, () -> Task.create("   ", "description"));
        }

        @Test
        @DisplayName("should throw exception when title exceeds 200 characters")
        void shouldThrowExceptionWhenTitleExceedsMaxLength() {
            // Arrange
            String longTitle = "a".repeat(201);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Task.create(longTitle, "description")
            );
            assertTrue(exception.getMessage().contains("200"), "Error message should mention character limit");
        }

        @Test
        @DisplayName("should accept title with exactly 200 characters")
        void shouldAcceptTitleWithMaxLength() {
            // Arrange
            String maxLengthTitle = "a".repeat(200);

            // Act
            Task task = Task.create(maxLengthTitle, "description");

            // Assert
            assertEquals(maxLengthTitle, task.getTitle());
        }

        @Test
        @DisplayName("should trim whitespace from title")
        void shouldTrimWhitespaceFromTitle() {
            // Arrange
            String titleWithWhitespace = "  Task Title  ";

            // Act
            Task task = Task.create(titleWithWhitespace, "description");

            // Assert
            assertEquals("Task Title", task.getTitle(), "Title should be trimmed");
        }
    }

    @Nested
    @DisplayName("State Transitions - Start")
    class StateTransitionsStart {

        @Test
        @DisplayName("should start task from PENDING status")
        void shouldStartTaskFromPending() {
            // Arrange
            Task task = Task.create("New task");

            // Act
            task.start();

            // Assert
            assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
            assertTrue(task.isInProgress());
        }

        @Test
        @DisplayName("should throw exception when starting already started task")
        void shouldThrowExceptionWhenStartingAlreadyStartedTask() {
            // Arrange
            Task task = Task.create("Task");
            task.start();

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                task::start
            );
            assertTrue(exception.getMessage().contains("Cannot start"));
        }

        @Test
        @DisplayName("should throw exception when starting completed task")
        void shouldThrowExceptionWhenStartingCompletedTask() {
            // Arrange
            Task task = Task.create("Task");
            task.complete();

            // Act & Assert
            assertThrows(IllegalStateException.class, task::start);
        }

        @Test
        @DisplayName("should throw exception when starting cancelled task")
        void shouldThrowExceptionWhenStartingCancelledTask() {
            // Arrange
            Task task = Task.create("Task");
            task.cancel();

            // Act & Assert
            assertThrows(IllegalStateException.class, task::start);
        }
    }

    @Nested
    @DisplayName("State Transitions - Complete")
    class StateTransitionsComplete {

        @Test
        @DisplayName("should complete task from PENDING status")
        void shouldCompleteTaskFromPending() {
            // Arrange
            Task task = Task.create("Task");

            // Act
            task.complete();

            // Assert
            assertEquals(TaskStatus.COMPLETED, task.getStatus());
            assertTrue(task.isCompleted());
            assertNotNull(task.getCompletedAt(), "Completed timestamp should be set");
        }

        @Test
        @DisplayName("should complete task from IN_PROGRESS status")
        void shouldCompleteTaskFromInProgress() {
            // Arrange
            Task task = Task.create("Task");
            task.start();

            // Act
            task.complete();

            // Assert
            assertEquals(TaskStatus.COMPLETED, task.getStatus());
            assertNotNull(task.getCompletedAt());
        }

        @Test
        @DisplayName("should throw exception when completing already completed task")
        void shouldThrowExceptionWhenCompletingAlreadyCompletedTask() {
            // Arrange
            Task task = Task.create("Task");
            task.complete();

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                task::complete
            );
            assertTrue(exception.getMessage().contains("already completed"));
        }

        @Test
        @DisplayName("should throw exception when completing cancelled task")
        void shouldThrowExceptionWhenCompletingCancelledTask() {
            // Arrange
            Task task = Task.create("Task");
            task.cancel();

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                task::complete
            );
            assertTrue(exception.getMessage().contains("cancelled"));
        }
    }

    @Nested
    @DisplayName("State Transitions - Cancel")
    class StateTransitionsCancel {

        @Test
        @DisplayName("should cancel task from PENDING status")
        void shouldCancelTaskFromPending() {
            // Arrange
            Task task = Task.create("Task");

            // Act
            task.cancel();

            // Assert
            assertEquals(TaskStatus.CANCELLED, task.getStatus());
            assertTrue(task.isCancelled());
        }

        @Test
        @DisplayName("should cancel task from IN_PROGRESS status")
        void shouldCancelTaskFromInProgress() {
            // Arrange
            Task task = Task.create("Task");
            task.start();

            // Act
            task.cancel();

            // Assert
            assertEquals(TaskStatus.CANCELLED, task.getStatus());
        }

        @Test
        @DisplayName("should throw exception when cancelling completed task")
        void shouldThrowExceptionWhenCancellingCompletedTask() {
            // Arrange
            Task task = Task.create("Task");
            task.complete();

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                task::cancel
            );
            assertTrue(exception.getMessage().contains("Cannot cancel a completed"));
        }

        @Test
        @DisplayName("should throw exception when cancelling already cancelled task")
        void shouldThrowExceptionWhenCancellingAlreadyCancelledTask() {
            // Arrange
            Task task = Task.create("Task");
            task.cancel();

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                task::cancel
            );
            assertTrue(exception.getMessage().contains("already cancelled"));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {

        @Test
        @DisplayName("should update title of PENDING task")
        void shouldUpdateTitleOfPendingTask() {
            // Arrange
            Task task = Task.create("Original title");
            String newTitle = "Updated title";

            // Act
            task.updateTitle(newTitle);

            // Assert
            assertEquals(newTitle, task.getTitle());
        }

        @Test
        @DisplayName("should update title of IN_PROGRESS task")
        void shouldUpdateTitleOfInProgressTask() {
            // Arrange
            Task task = Task.create("Original title");
            task.start();
            String newTitle = "Updated title";

            // Act
            task.updateTitle(newTitle);

            // Assert
            assertEquals(newTitle, task.getTitle());
        }

        @Test
        @DisplayName("should throw exception when updating title of completed task")
        void shouldThrowExceptionWhenUpdatingTitleOfCompletedTask() {
            // Arrange
            Task task = Task.create("Task");
            task.complete();

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> task.updateTitle("New title")
            );
            assertTrue(exception.getMessage().contains("Cannot update title"));
        }

        @Test
        @DisplayName("should throw exception when updating title of cancelled task")
        void shouldThrowExceptionWhenUpdatingTitleOfCancelledTask() {
            // Arrange
            Task task = Task.create("Task");
            task.cancel();

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> task.updateTitle("New title"));
        }

        @Test
        @DisplayName("should validate new title when updating")
        void shouldValidateNewTitleWhenUpdating() {
            // Arrange
            Task task = Task.create("Task");

            // Act & Assert
            assertThrows(NullPointerException.class, () -> task.updateTitle(null));
            assertThrows(IllegalArgumentException.class, () -> task.updateTitle(""));
            assertThrows(IllegalArgumentException.class, () -> task.updateTitle("   "));
            assertThrows(IllegalArgumentException.class, () -> task.updateTitle("a".repeat(201)));
        }

        @Test
        @DisplayName("should update description of PENDING task")
        void shouldUpdateDescriptionOfPendingTask() {
            // Arrange
            Task task = Task.create("Task", "Original description");
            String newDescription = "Updated description";

            // Act
            task.updateDescription(newDescription);

            // Assert
            assertEquals(newDescription, task.getDescription());
        }

        @Test
        @DisplayName("should update description of IN_PROGRESS task")
        void shouldUpdateDescriptionOfInProgressTask() {
            // Arrange
            Task task = Task.create("Task", "Original");
            task.start();
            String newDescription = "Updated";

            // Act
            task.updateDescription(newDescription);

            // Assert
            assertEquals(newDescription, task.getDescription());
        }

        @Test
        @DisplayName("should allow null description")
        void shouldAllowNullDescription() {
            // Arrange
            Task task = Task.create("Task", "Description");

            // Act
            task.updateDescription(null);

            // Assert
            assertNull(task.getDescription());
        }

        @Test
        @DisplayName("should throw exception when updating description of completed task")
        void shouldThrowExceptionWhenUpdatingDescriptionOfCompletedTask() {
            // Arrange
            Task task = Task.create("Task");
            task.complete();

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> task.updateDescription("New"));
        }

        @Test
        @DisplayName("should throw exception when updating description of cancelled task")
        void shouldThrowExceptionWhenUpdatingDescriptionOfCancelledTask() {
            // Arrange
            Task task = Task.create("Task");
            task.cancel();

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> task.updateDescription("New"));
        }
    }

    @Nested
    @DisplayName("State Query Methods")
    class StateQueryMethods {

        @Test
        @DisplayName("isCompleted should return true only for completed tasks")
        void isCompletedShouldReturnTrueOnlyForCompletedTasks() {
            Task pendingTask = Task.create("Task");
            assertFalse(pendingTask.isCompleted());

            Task inProgressTask = Task.create("Task");
            inProgressTask.start();
            assertFalse(inProgressTask.isCompleted());

            Task completedTask = Task.create("Task");
            completedTask.complete();
            assertTrue(completedTask.isCompleted());

            Task cancelledTask = Task.create("Task");
            cancelledTask.cancel();
            assertFalse(cancelledTask.isCompleted());
        }

        @Test
        @DisplayName("isCancelled should return true only for cancelled tasks")
        void isCancelledShouldReturnTrueOnlyForCancelledTasks() {
            Task pendingTask = Task.create("Task");
            assertFalse(pendingTask.isCancelled());

            Task inProgressTask = Task.create("Task");
            inProgressTask.start();
            assertFalse(inProgressTask.isCancelled());

            Task completedTask = Task.create("Task");
            completedTask.complete();
            assertFalse(completedTask.isCancelled());

            Task cancelledTask = Task.create("Task");
            cancelledTask.cancel();
            assertTrue(cancelledTask.isCancelled());
        }

        @Test
        @DisplayName("isInProgress should return true only for in-progress tasks")
        void isInProgressShouldReturnTrueOnlyForInProgressTasks() {
            Task pendingTask = Task.create("Task");
            assertFalse(pendingTask.isInProgress());

            Task inProgressTask = Task.create("Task");
            inProgressTask.start();
            assertTrue(inProgressTask.isInProgress());

            Task completedTask = Task.create("Task");
            completedTask.complete();
            assertFalse(completedTask.isInProgress());

            Task cancelledTask = Task.create("Task");
            cancelledTask.cancel();
            assertFalse(cancelledTask.isInProgress());
        }
    }

    @Nested
    @DisplayName("Getters and Immutability")
    class GettersAndImmutability {

        @Test
        @DisplayName("should provide access to all task properties")
        void shouldProvideAccessToAllTaskProperties() {
            // Arrange & Act
            Task task = Task.create("Task title", "Task description");

            // Assert
            assertNotNull(task.getId());
            assertNotNull(task.getTitle());
            assertNotNull(task.getDescription());
            assertNotNull(task.getStatus());
            assertNotNull(task.getCreatedAt());
            assertNull(task.getCompletedAt()); // Not completed yet
        }

        @Test
        @DisplayName("should generate unique IDs for different tasks")
        void shouldGenerateUniqueIdsForDifferentTasks() {
            // Arrange & Act
            Task task1 = Task.create("Task 1");
            Task task2 = Task.create("Task 2");

            // Assert
            assertNotEquals(task1.getId(), task2.getId());
        }

        @Test
        @DisplayName("completedAt should be null for non-completed tasks")
        void completedAtShouldBeNullForNonCompletedTasks() {
            Task pendingTask = Task.create("Task");
            assertNull(pendingTask.getCompletedAt());

            Task inProgressTask = Task.create("Task");
            inProgressTask.start();
            assertNull(inProgressTask.getCompletedAt());

            Task cancelledTask = Task.create("Task");
            cancelledTask.cancel();
            assertNull(cancelledTask.getCompletedAt());
        }
    }
}
