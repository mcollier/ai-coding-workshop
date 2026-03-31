# Task Manager - Spring Boot Implementation

A production-ready RESTful API for task management built with Spring Boot 3.x, following Clean Architecture and Domain-Driven Design principles.

## 🏗️ Architecture

This implementation follows **Clean Architecture** (Hexagonal Architecture) with clear separation of concerns:

```
taskmanager-domain         → Pure Java, no dependencies (business logic)
taskmanager-application    → Spring context only (use cases, orchestration)
taskmanager-infrastructure → Spring Data JPA (database persistence)
taskmanager-api            → Spring Web (REST controllers, DTOs)
```

### Design Principles

- **Domain-Driven Design (DDD)**: Aggregates, value objects, domain events
- **Clean Architecture**: Dependency inversion, ports & adapters
- **SOLID Principles**: Single responsibility, interface segregation
- **Test-Driven Development**: 102 tests with comprehensive coverage

## 🚀 Quick Start

### Prerequisites

- **Java 21** (LTS)
- **Maven 3.9+**
- **PostgreSQL 16+** (or use H2 for development)

### Build and Run

```bash
# Navigate to Spring Boot project
cd src-springboot

# Build all modules
mvn clean install

# Run the application
mvn spring-boot:run -pl taskmanager-api

# Or run with specific profile
mvn spring-boot:run -pl taskmanager-api -Dspring-boot.run.profiles=dev
```

The application will start on `http://localhost:8080`

### Access API Documentation

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

### Run Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

## 📦 Project Structure

```
src-springboot/
├── pom.xml                          # Parent POM with dependency management
├── taskmanager-domain/              # Domain Layer (Pure Java)
│   ├── src/main/java/
│   │   └── com/example/taskmanager/domain/
│   │       └── tasks/
│   │           ├── Task.java        # Aggregate root
│   │           ├── TaskId.java      # Value object (strongly-typed ID)
│   │           ├── TaskStatus.java  # Enum
│   │           └── TaskRepository.java  # Port interface
│   └── src/test/java/               # 36 unit tests
│
├── taskmanager-application/         # Application Layer (Use Cases)
│   ├── src/main/java/
│   │   └── com/example/taskmanager/application/
│   │       └── services/
│   │           └── TaskService.java # Application service (@Transactional)
│   └── src/test/java/               # 33 unit tests (Mockito)
│
├── taskmanager-infrastructure/      # Infrastructure Layer (Adapters)
│   ├── src/main/java/
│   │   └── com/example/taskmanager/infrastructure/
│   │       └── persistence/
│   │           ├── entities/
│   │           │   └── TaskEntity.java          # JPA entity
│   │           ├── mappers/
│   │           │   └── TaskMapper.java          # Domain ↔ Entity mapper
│   │           └── repositories/
│   │               ├── SpringDataTaskRepository.java  # JPA repo interface
│   │               └── JpaTaskRepositoryAdapter.java  # Adapter implementation
│   └── src/test/java/               # 25 integration tests (H2)
│
└── taskmanager-api/                 # API Layer (Controllers)
    ├── src/main/java/
    │   └── com/example/taskmanager/api/
│       ├── TaskManagerApplication.java   # Main application class
    │       ├── config/
    │       │   └── OpenApiConfig.java    # OpenAPI configuration
    │       ├── controllers/
    │       │   └── TaskController.java   # REST endpoints
    │       ├── dto/
    │       │   ├── CreateTaskRequest.java
    │       │   ├── UpdateTaskRequest.java
    │       │   └── TaskResponse.java
    │       └── exception/
    │           └── GlobalExceptionHandler.java  # RFC 7807 error handling
    ├── src/main/resources/
    │   ├── application.yml           # Base configuration
    │   ├── application-dev.yml       # Development overrides
    │   ├── application-prod.yml      # Production overrides
    │   └── application-test.yml      # Test overrides
    └── src/test/java/                # 8 API integration tests
```

## 🎯 API Endpoints

### Tasks

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/tasks` | Create a new task |
| `GET` | `/api/tasks` | List all tasks |
| `GET` | `/api/tasks/{id}` | Get task by ID |
| `PATCH` | `/api/tasks/{id}` | Update task (partial) |
| `PUT` | `/api/tasks/{id}/start` | Start task (PENDING → IN_PROGRESS) |
| `PUT` | `/api/tasks/{id}/complete` | Complete task (→ COMPLETED) |
| `PUT` | `/api/tasks/{id}/cancel` | Cancel task (→ CANCELLED) |
| `DELETE` | `/api/tasks/{id}` | Delete task |

### Example Requests

**Create Task**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete Spring Boot implementation",
    "description": "Add all remaining features"
  }'
```

**Start Task**
```bash
curl -X PUT http://localhost:8080/api/tasks/{id}/start
```

**List All Tasks**
```bash
curl http://localhost:8080/api/tasks
```

## 🔧 Configuration

### Database Configuration

#### Development (H2 In-Memory)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
```

#### Production (PostgreSQL)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskmanager
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

### Environment Profiles

Activate profiles using:
```bash
# Development
java -jar taskmanager-api.jar --spring.profiles.active=dev

# Production
java -jar taskmanager-api.jar --spring.profiles.active=prod
```

## 🧪 Testing Strategy

### Test Coverage

- **Domain Layer**: 36 unit tests (business logic, invariants, state transitions) - **79.94% coverage**
- **Application Layer**: 33 unit tests (service orchestration, Mockito) - **100% coverage**
- **Infrastructure Layer**: 25 tests (JPA integration, mappers, H2 database) - **44.05% coverage**
- **API Layer**: 8 integration tests (full stack, MockMvc) - **82.89% coverage**

**Total: 102 tests, 0 failures, 72.55% overall instruction coverage** ✅

> **Note**: Infrastructure coverage is lower due to JPA entity boilerplate and Spring Data repository methods that are tested functionally through integration tests rather than unit tests.

### Test Types

1. **Unit Tests** - Fast, isolated, no Spring context
   - Domain: Pure Java, test aggregates and value objects
   - Application: Mockito for repository mocking

2. **Integration Tests** - Spring context with H2
   - Infrastructure: Test JPA entities and repositories
   - API: Full HTTP request/response cycle

### Running Specific Test Suites

```bash
# Domain tests only
mvn test -pl taskmanager-domain

# Infrastructure integration tests
mvn test -pl taskmanager-infrastructure

# API integration tests
mvn test -pl taskmanager-api
```

## 📊 Monitoring & Observability

### Actuator Endpoints

- **Health**: `http://localhost:8080/actuator/health`
- **Info**: `http://localhost:8080/actuator/info`
- **Metrics**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`

### Logging

Logs are configured per environment:

- **Development**: DEBUG level, colorized console output
- **Production**: WARN level, file-based logging with rotation
- **Test**: Minimal logging to reduce test output noise

## 🔐 Error Handling

All errors follow **RFC 7807 Problem Details** format:

```json
{
  "type": "https://api.taskmanager.example.com/problems/task-not-found",
  "title": "Task Not Found",
  "status": 404,
  "detail": "Task not found with ID: 123e4567-e89b-12d3-a456-426614174000",
  "taskId": "123e4567-e89b-12d3-a456-426614174000"
}
```

### Error Types

- `400 Bad Request` - Validation errors, invalid state transitions
- `404 Not Found` - Task not found
- `500 Internal Server Error` - Unexpected errors (logged)

## 🛠️ Development

### Adding New Features

Follow Clean Architecture boundaries:

1. **Domain** - Add business logic, aggregates, value objects
2. **Application** - Create use case services
3. **Infrastructure** - Implement persistence adapters
4. **API** - Expose REST endpoints with DTOs

### Code Style

- Java 21 features (records, pattern matching, sealed types)
- Constructor injection (Lombok `@RequiredArgsConstructor`)
- Final classes by default (prevent inheritance unless intended)
- No primitive obsession (use value objects)

### Testing Requirements

- All new features must have tests
- Maintain 70%+ code coverage
- Follow Arrange-Act-Assert pattern
- Use `@DisplayName` for readable test names

## 📚 Technology Stack

- **Java 21** (LTS)
- **Spring Boot 3.2.4**
- **Spring Data JPA** + **Hibernate**
- **PostgreSQL 16** (production) / **H2** (tests)
- **Springdoc OpenAPI** (Swagger documentation)
- **JUnit 5** + **Mockito** (testing)
- **Maven 3.9+** (build tool)
- **Lombok** (boilerplate reduction)
- **SLF4J + Logback** (logging)

## 🎓 Learning Resources

This implementation demonstrates:

- **Clean Architecture** patterns in practice
- **Domain-Driven Design** tactical patterns
- **Hexagonal Architecture** (Ports & Adapters)
- **Test-Driven Development** with comprehensive test coverage
- **Spring Boot 3.x** best practices
- **RESTful API** design with proper HTTP semantics
- **OpenAPI** documentation standards

## 📝 License

This is a workshop/training implementation. See the main repository for license details.

## 🤝 Contributing

This is part of the AI Code Workshop. For contributions, see the main repository's contributing guidelines.

---

**Workshop Context**: This Spring Boot implementation serves as a reference for Clean Architecture and DDD principles in a modern Java application. It complements the existing .NET Core implementation in the workshop.
