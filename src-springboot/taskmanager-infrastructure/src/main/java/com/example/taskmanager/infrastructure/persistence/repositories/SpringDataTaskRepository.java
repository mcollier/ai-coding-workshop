package com.example.taskmanager.infrastructure.persistence.repositories;

import com.example.taskmanager.infrastructure.persistence.entities.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for TaskEntity.
 * Provides CRUD operations and custom query methods.
 * 
 * This is the Spring Data interface - the infrastructure adapter
 * that Spring Data JPA will implement at runtime.
 */
interface SpringDataTaskRepository extends JpaRepository<TaskEntity, UUID> {
    
    /**
     * Find all tasks by status.
     * Uses Spring Data method query derivation.
     * 
     * @param status the task status string
     * @return list of task entities with matching status
     */
    List<TaskEntity> findByStatus(String status);
    
    /**
     * Count tasks by status.
     * 
     * @param status the task status string
     * @return count of tasks with matching status
     */
    long countByStatus(String status);
}
