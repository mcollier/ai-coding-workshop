package com.example.taskmanager.api;

import com.example.taskmanager.api.dto.CreateTaskRequest;
import com.example.taskmanager.api.dto.TaskResponse;
import com.example.taskmanager.api.dto.UpdateTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Integration tests for Task management.
 * Uses full Spring Boot context with H2 in-memory database.
 * 
 * Each test runs in a transaction that rolls back after completion (@Transactional),
 * ensuring test isolation and a clean database for each test.
 * 
 * Tests verify:
 * - End-to-end API flows work correctly
 * - Database persistence through all layers
 * - REST endpoints integrate with services, repositories, and database
 * - Exception handling works in full context
 * 
 * Note: Uses H2 for fast test execution. For production-like testing with
 * real PostgreSQL, use Testcontainers (requires Docker).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
@DisplayName("API Integration Tests")
class TaskApiIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        // Database is recreated for each test (create-drop)
    }
    
    @Test
    @DisplayName("Should create, retrieve, update, and delete task (full lifecycle)")
    void shouldPerformFullTaskLifecycle() throws Exception {
        // CREATE task
        CreateTaskRequest createRequest = new CreateTaskRequest("Integration Test Task", "Test Description");
        
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        
        TaskResponse createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskResponse.class
        );
        UUID taskId = createdTask.id();
        
        // RETRIEVE task
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.description").value("Test Description"));
        
        // UPDATE task title
        UpdateTaskRequest updateRequest = new UpdateTaskRequest("Updated Title", null);
        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Test Description"));
        
        // DELETE task
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());
        
        // VERIFY task is deleted
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should handle task state transitions through API")
    void shouldHandleTaskStateTransitions() throws Exception {
        // Create task
        CreateTaskRequest createRequest = new CreateTaskRequest("State Transition Task", null);
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        TaskResponse createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskResponse.class
        );
        UUID taskId = createdTask.id();
        
        // Start task (PENDING → IN_PROGRESS)
        mockMvc.perform(put("/api/tasks/{id}/start", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        
        // Complete task (IN_PROGRESS → COMPLETED)
        mockMvc.perform(put("/api/tasks/{id}/complete", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
        
        // Verify completed task persisted
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }
    
    @Test
    @DisplayName("Should list multiple tasks")
    void shouldListMultipleTasks() throws Exception {
        // Create multiple tasks
        CreateTaskRequest request1 = new CreateTaskRequest("Task 1", "Description 1");
        CreateTaskRequest request2 = new CreateTaskRequest("Task 2", "Description 2");
        CreateTaskRequest request3 = new CreateTaskRequest("Task 3", null);
        
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isCreated());
        
        // List all tasks
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("Task 1", "Task 2", "Task 3")));
    }
    
    @Test
    @DisplayName("Should reject invalid task creation")
    void shouldRejectInvalidTaskCreation() throws Exception {
        // Blank title
        CreateTaskRequest blankTitleRequest = new CreateTaskRequest("", "Description");
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankTitleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.title").exists());
        
        // Title too long
        String longTitle = "a".repeat(201);
        CreateTaskRequest longTitleRequest = new CreateTaskRequest(longTitle, null);
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(longTitleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.errors.title").exists());
    }
    
    @Test
    @DisplayName("Should return 404 for non-existent task")
    void shouldReturn404ForNonExistentTask() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        
        // GET non-existent task
        mockMvc.perform(get("/api/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound());
        
        // UPDATE non-existent task
        UpdateTaskRequest updateRequest = new UpdateTaskRequest("Title", null);
        mockMvc.perform(patch("/api/tasks/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Task Not Found"))
                .andExpect(jsonPath("$.taskId").value(nonExistentId.toString()));
        
        // DELETE non-existent task
        mockMvc.perform(delete("/api/tasks/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Should handle business rule violations")
    void shouldHandleBusinessRuleViolations() throws Exception {
        // Create and complete a task
        CreateTaskRequest createRequest = new CreateTaskRequest("Business Rule Task", null);
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        TaskResponse createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskResponse.class
        );
        UUID taskId = createdTask.id();
        
        // Complete the task
        mockMvc.perform(put("/api/tasks/{id}/complete", taskId))
                .andExpect(status().isOk());
        
        // Try to start a completed task (should fail)
        mockMvc.perform(put("/api/tasks/{id}/start", taskId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Operation"))
                .andExpect(jsonPath("$.detail", containsString("Cannot start")));
        
        // Try to update a completed task (should fail)
        UpdateTaskRequest updateRequest = new UpdateTaskRequest("New Title", null);
        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Operation"));
    }
    
    @Test
    @DisplayName("Should cancel task successfully")
    void shouldCancelTaskSuccessfully() throws Exception {
        // Create task
        CreateTaskRequest createRequest = new CreateTaskRequest("Cancel Test Task", null);
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        TaskResponse createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskResponse.class
        );
        UUID taskId = createdTask.id();
        
        // Cancel task
        mockMvc.perform(put("/api/tasks/{id}/cancel", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        
        // Verify cannot complete cancelled task
        mockMvc.perform(put("/api/tasks/{id}/complete", taskId))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("Should update task description")
    void shouldUpdateTaskDescription() throws Exception {
        // Create task
        CreateTaskRequest createRequest = new CreateTaskRequest("Description Update Task", "Original");
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        
        TaskResponse createdTask = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                TaskResponse.class
        );
        UUID taskId = createdTask.id();
        
        // Update description
        UpdateTaskRequest updateRequest = new UpdateTaskRequest(null, "Updated Description");
        mockMvc.perform(patch("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Description Update Task"))
                .andExpect(jsonPath("$.description").value("Updated Description"));
    }
}
