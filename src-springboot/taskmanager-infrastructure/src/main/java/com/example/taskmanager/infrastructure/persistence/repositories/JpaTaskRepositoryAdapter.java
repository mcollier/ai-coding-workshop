package com.example.taskmanager.infrastructure.persistence.repositories;

import com.example.taskmanager.domain.tasks.Task;
import com.example.taskmanager.domain.tasks.TaskId;
import com.example.taskmanager.domain.tasks.TaskRepository;
import com.example.taskmanager.domain.tasks.TaskStatus;
import com.example.taskmanager.infrastructure.persistence.entities.TaskEntity;
import com.example.taskmanager.infrastructure.persistence.mappers.TaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA implementation of TaskRepository (domain interface).
 * Adapts Spring Data JPA repository to domain repository contract.
 * 
 * This is the infrastructure adapter that bridges the domain layer
 * (TaskRepository interface) with the persistence layer (Spring Data JPA).
 * 
 * Uses TaskMapper to translate between domain objects and JPA entities.
 */
@Repository
@RequiredArgsConstructor
public final class JpaTaskRepositoryAdapter implements TaskRepository {
    
    private final SpringDataTaskRepository jpaRepository;
    private final TaskMapper mapper;
    
    @Override
    public Task save(Task task) {
        TaskEntity entity = mapper.toEntity(task);
        TaskEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<Task> findById(TaskId id) {
        return jpaRepository.findById(id.value())
            .map(mapper::toDomain);
    }
    
    @Override
    public List<Task> findByStatus(TaskStatus status) {
        return jpaRepository.findByStatus(status.name())
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Task> findAll() {
        return jpaRepository.findAll()
            .stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void deleteById(TaskId id) {
        jpaRepository.deleteById(id.value());
    }
    
    @Override
    public boolean existsById(TaskId id) {
        return jpaRepository.existsById(id.value());
    }
    
    @Override
    public long count() {
        return jpaRepository.count();
    }
    
    @Override
    public long countByStatus(TaskStatus status) {
        return jpaRepository.countByStatus(status.name());
    }
}
