package com.example.taskmanager.application.services;

import com.example.taskmanager.application.services.TaskService.TaskNotFoundException;
import com.example.taskmanager.domain.tasks.Task;
import com.example.taskmanager.domain.tasks.TaskId;
import com.example.taskmanager.domain.tasks.TaskRepository;
import com.example.taskmanager.domain.tasks.TaskStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 * Tests service orchestration logic and repository interactions using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Nested
    @DisplayName("Create Task Operations")
    class CreateTaskOperations {

        @Test
        @DisplayName("should create task with title only")
        void shouldCreateTaskWithTitleOnly() {
            // Arrange
            String title = "New task";
            Task expectedTask = Task.create(title);
            when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);

            // Act
            Task result = taskService.createTask(title);

            // Assert
            assertNotNull(result);
            assertEquals(title, result.getTitle());
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should create task with title and description")
        void shouldCreateTaskWithTitleAndDescription() {
            // Arrange
            String title = "New task";
            String description = "Task description";
            Task expectedTask = Task.create(title, description);
            when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);

            // Act
            Task result = taskService.createTask(title, description);

            // Assert
            assertNotNull(result);
            assertEquals(title, result.getTitle());
            assertEquals(description, result.getDescription());
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when creating task with null title")
        void shouldThrowExceptionWhenCreatingTaskWithNullTitle() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> taskService.createTask(null));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when creating task with blank title")
        void shouldThrowExceptionWhenCreatingTaskWithBlankTitle() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> taskService.createTask("   "));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should save task to repository when creating")
        void shouldSaveTaskToRepositoryWhenCreating() {
            // Arrange
            String title = "Task";
            Task task = Task.create(title);
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            taskService.createTask(title);

            // Assert
            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Find Task Operations")
    class FindTaskOperations {

        @Test
        @DisplayName("should find task by ID when it exists")
        void shouldFindTaskByIdWhenItExists() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Test task");
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // Act
            Optional<Task> result = taskService.findTask(taskId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("Test task", result.get().getTitle());
            verify(taskRepository, times(1)).findById(taskId);
        }

        @Test
        @DisplayName("should return empty when task not found")
        void shouldReturnEmptyWhenTaskNotFound() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // Act
            Optional<Task> result = taskService.findTask(taskId);

            // Assert
            assertFalse(result.isPresent());
            verify(taskRepository, times(1)).findById(taskId);
        }

        @Test
        @DisplayName("should find all tasks")
        void shouldFindAllTasks() {
            // Arrange
            List<Task> tasks = List.of(
                Task.create("Task 1"),
                Task.create("Task 2"),
                Task.create("Task 3")
            );
            when(taskRepository.findAll()).thenReturn(tasks);

            // Act
            List<Task> result = taskService.findAllTasks();

            // Assert
            assertEquals(3, result.size());
            verify(taskRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("should find tasks by status")
        void shouldFindTasksByStatus() {
            // Arrange
            TaskStatus status = TaskStatus.PENDING;
            List<Task> pendingTasks = List.of(
                Task.create("Pending 1"),
                Task.create("Pending 2")
            );
            when(taskRepository.findByStatus(status)).thenReturn(pendingTasks);

            // Act
            List<Task> result = taskService.findTasksByStatus(status);

            // Assert
            assertEquals(2, result.size());
            verify(taskRepository, times(1)).findByStatus(status);
        }
    }

    @Nested
    @DisplayName("Start Task Operations")
    class StartTaskOperations {

        @Test
        @DisplayName("should start task when it exists and is pending")
        void shouldStartTaskWhenItExistsAndIsPending() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Test task");
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.startTask(taskId);

            // Assert
            assertEquals(TaskStatus.IN_PROGRESS, result.getStatus());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task not found")
        void shouldThrowTaskNotFoundExceptionWhenTaskNotFound() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // Act & Assert
            TaskNotFoundException exception = assertThrows(
                TaskNotFoundException.class,
                () -> taskService.startTask(taskId)
            );
            assertEquals(taskId, exception.getTaskId());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when starting non-pending task")
        void shouldThrowExceptionWhenStartingNonPendingTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Task");
            task.start(); // Already in progress
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> taskService.startTask(taskId));
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Complete Task Operations")
    class CompleteTaskOperations {

        @Test
        @DisplayName("should complete pending task")
        void shouldCompletePendingTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Test task");
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.completeTask(taskId);

            // Assert
            assertTrue(result.isCompleted());
            assertNotNull(result.getCompletedAt());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should complete in-progress task")
        void shouldCompleteInProgressTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Test task");
            task.start();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.completeTask(taskId);

            // Assert
            assertTrue(result.isCompleted());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task not found")
        void shouldThrowTaskNotFoundExceptionWhenTaskNotFoundOnComplete() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(TaskNotFoundException.class, () -> taskService.completeTask(taskId));
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when completing cancelled task")
        void shouldThrowExceptionWhenCompletingCancelledTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Task");
            task.cancel();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> taskService.completeTask(taskId));
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Cancel Task Operations")
    class CancelTaskOperations {

        @Test
        @DisplayName("should cancel pending task")
        void shouldCancelPendingTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Test task");
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.cancelTask(taskId);

            // Assert
            assertTrue(result.isCancelled());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should cancel in-progress task")
        void shouldCancelInProgressTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Test task");
            task.start();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.cancelTask(taskId);

            // Assert
            assertTrue(result.isCancelled());
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when task not found")
        void shouldThrowTaskNotFoundExceptionWhenTaskNotFoundOnCancel() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(TaskNotFoundException.class, () -> taskService.cancelTask(taskId));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when cancelling completed task")
        void shouldThrowExceptionWhenCancellingCompletedTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Task");
            task.complete();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> taskService.cancelTask(taskId));
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Update Task Operations")
    class UpdateTaskOperations {

        @Test
        @DisplayName("should update task title")
        void shouldUpdateTaskTitle() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Original title");
            String newTitle = "Updated title";
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.updateTaskTitle(taskId, newTitle);

            // Assert
            assertEquals(newTitle, result.getTitle());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when updating title of non-existent task")
        void shouldThrowTaskNotFoundExceptionWhenUpdatingTitleOfNonExistentTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(TaskNotFoundException.class, 
                () -> taskService.updateTaskTitle(taskId, "New title"));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when updating title with invalid value")
        void shouldThrowExceptionWhenUpdatingTitleWithInvalidValue() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Original");
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // Act & Assert
            assertThrows(NullPointerException.class, 
                () -> taskService.updateTaskTitle(taskId, null));
            assertThrows(IllegalArgumentException.class, 
                () -> taskService.updateTaskTitle(taskId, ""));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw exception when updating title of completed task")
        void shouldThrowExceptionWhenUpdatingTitleOfCompletedTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Title");
            task.complete();
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

            // Act & Assert
            assertThrows(IllegalStateException.class, 
                () -> taskService.updateTaskTitle(taskId, "New title"));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("should update task description")
        void shouldUpdateTaskDescription() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Title", "Original description");
            String newDescription = "Updated description";
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.updateTaskDescription(taskId, newDescription);

            // Assert
            assertEquals(newDescription, result.getDescription());
            verify(taskRepository, times(1)).findById(taskId);
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should allow null description when updating")
        void shouldAllowNullDescriptionWhenUpdating() {
            // Arrange
            TaskId taskId = TaskId.newId();
            Task task = Task.create("Title", "Description");
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // Act
            Task result = taskService.updateTaskDescription(taskId, null);

            // Assert
            assertNull(result.getDescription());
            verify(taskRepository, times(1)).save(task);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when updating description of non-existent task")
        void shouldThrowTaskNotFoundExceptionWhenUpdatingDescriptionOfNonExistentTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(TaskNotFoundException.class, 
                () -> taskService.updateTaskDescription(taskId, "New description"));
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Delete Task Operations")
    class DeleteTaskOperations {

        @Test
        @DisplayName("should delete existing task")
        void shouldDeleteExistingTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.existsById(taskId)).thenReturn(true);
            doNothing().when(taskRepository).deleteById(taskId);

            // Act
            taskService.deleteTask(taskId);

            // Assert
            verify(taskRepository, times(1)).existsById(taskId);
            verify(taskRepository, times(1)).deleteById(taskId);
        }

        @Test
        @DisplayName("should throw TaskNotFoundException when deleting non-existent task")
        void shouldThrowTaskNotFoundExceptionWhenDeletingNonExistentTask() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.existsById(taskId)).thenReturn(false);

            // Act & Assert
            assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(taskId));
            verify(taskRepository, times(1)).existsById(taskId);
            verify(taskRepository, never()).deleteById(any(TaskId.class));
        }
    }

    @Nested
    @DisplayName("Task Existence Checks")
    class TaskExistenceChecks {

        @Test
        @DisplayName("should return true when task exists")
        void shouldReturnTrueWhenTaskExists() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.existsById(taskId)).thenReturn(true);

            // Act
            boolean result = taskService.taskExists(taskId);

            // Assert
            assertTrue(result);
            verify(taskRepository, times(1)).existsById(taskId);
        }

        @Test
        @DisplayName("should return false when task does not exist")
        void shouldReturnFalseWhenTaskDoesNotExist() {
            // Arrange
            TaskId taskId = TaskId.newId();
            when(taskRepository.existsById(taskId)).thenReturn(false);

            // Act
            boolean result = taskService.taskExists(taskId);

            // Assert
            assertFalse(result);
            verify(taskRepository, times(1)).existsById(taskId);
        }
    }

    @Nested
    @DisplayName("TaskNotFoundException Behavior")
    class TaskNotFoundExceptionBehavior {

        @Test
        @DisplayName("should include task ID in exception")
        void shouldIncludeTaskIdInException() {
            // Arrange
            TaskId taskId = TaskId.newId();

            // Act
            TaskNotFoundException exception = new TaskNotFoundException(taskId);

            // Assert
            assertEquals(taskId, exception.getTaskId());
            assertTrue(exception.getMessage().contains(taskId.value().toString()));
        }

        @Test
        @DisplayName("should have descriptive error message")
        void shouldHaveDescriptiveErrorMessage() {
            // Arrange
            TaskId taskId = TaskId.newId();

            // Act
            TaskNotFoundException exception = new TaskNotFoundException(taskId);

            // Assert
            assertTrue(exception.getMessage().contains("Task not found"));
            assertTrue(exception.getMessage().contains(taskId.value().toString()));
        }
    }
}
