# GitHub Copilot Instructions for Spring Boot Workshop

> These instructions are automatically applied to all GitHub Copilot suggestions in this repository when working with Spring Boot code.

## 0) Workshop Mode
- Assume **Spring Boot 3.x**, **Java 21**, **JUnit 5**, **Mockito**, **Spring Web (REST)**, **SLF4J**, **reactive patterns** where applicable.
- Prefer **Clean Architecture** project layout and **DDD** patterns.
- Always generate examples and code in **English**.

---

## 1) Workflow (TDD + Build Hygiene)
- **TDD first**: when asked to implement a feature, propose/emit tests before code.
- After you output code, assume we run `mvn clean test` or `gradle test` and fix warnings/errors before committing.
- When referencing rule sets, state what you followed (e.g., "Used: Clean Architecture, DDD, Tests").

---

## 2) Project Architecture (Clean Architecture)
Generate and maintain the following multi-module structure:  
`[project]-domain`, `[project]-application`, `[project]-infrastructure`, `[project]-api`, plus test modules. Enforce dependencies:  
- **Domain** → no deps (pure Java, no Spring)
- **Application** → Domain only  
- **Infrastructure** → Application + Domain + Spring Data JPA  
- **Api** → Infrastructure only + Spring Web  
Prefer **feature-oriented packages** (e.g., `order/`, `customer/`) instead of technical groupings.

Implementation guidance:
- **Domain**: business logic, entities, value objects, domain events. No Spring dependencies.  
- **Application**: use cases, command/query handlers, port interfaces (repositories, services).  
- **Infrastructure**: adapters, JPA entities, Spring Data repositories, external integrations.  
- **Api**: REST controllers + request/response DTOs only. No business logic.  
- Use Spring DI; avoid circular dependencies; no CQRS libraries for this workshop.

---

## 3) Java Coding Style (Java/Spring Boot conventions)
- One class per file; 4-space indent; `PascalCase` for classes, `camelCase` for methods/variables; constants `UPPER_SNAKE_CASE`.  
- Prefer clarity over brevity; use meaningful names; **make classes `final` by default** unless inheritance is intentional.
- Use Java 21 features: records, pattern matching, sealed types where appropriate.
- Prefer constructor injection over field injection; use `@RequiredArgsConstructor` (Lombok) or explicit constructors.
- Use `Optional<T>` for nullable return types; avoid returning null.

**Package naming**: `com.example.[project].[layer].[feature]`
- Domain: `com.example.taskmanager.domain.tasks`
- Application: `com.example.taskmanager.application.tasks.commands`
- Infrastructure: `com.example.taskmanager.infrastructure.persistence`
- Api: `com.example.taskmanager.api.controllers`

---

## 4) DDD Modeling Rules
- Model **Aggregates** with factory methods (private constructors), encapsulate invariants, and avoid direct navigation to other aggregates.  
- **Entities** live inside aggregates; no public setters; lifecycle managed by the root.  
- **Value Objects** are immutable Java records; implement value equality automatically.  
- Prefer **Strongly-Typed IDs** (e.g., `OrderId` as record) instead of primitive `Long` or `UUID`.  
- **Repositories** are interfaces in Application layer with **business-intent method names** (no generic CRUD verbs in domain).

**Method naming**: favor ubiquitous language (e.g., `placeOrder()`, `markAsShipped()`) over `create/update/delete/get`.

**Example - Strongly-Typed ID:**
```java
public record TaskId(UUID value) {
    public TaskId {
        Objects.requireNonNull(value, "TaskId cannot be null");
    }
    
    public static TaskId newId() {
        return new TaskId(UUID.randomUUID());
    }
}
```

**Example - Domain Entity:**
```java
public final class Task {
    private final TaskId id;
    private String title;
    private TaskStatus status;
    
    // Private constructor - use factory method
    private Task(TaskId id, String title) {
        this.id = Objects.requireNonNull(id);
        this.title = validateTitle(title);
        this.status = TaskStatus.PENDING;
    }
    
    // Factory method
    public static Task create(String title) {
        return new Task(TaskId.newId(), title);
    }
    
    // Business method with invariant enforcement
    public void complete() {
        if (status == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Task already completed");
        }
        this.status = TaskStatus.COMPLETED;
    }
    
    // Getters only - no setters
    public TaskId getId() { return id; }
    public String getTitle() { return title; }
    public TaskStatus getStatus() { return status; }
}
```

---

## 5) Testing Rules
- **Test framework**: `JUnit 5` (Jupiter).  
- **Mocks**: `Mockito` (avoid PowerMock).  
- **Integration**: `@SpringBootTest` + `Testcontainers` for database/infrastructure tests.  
- Unit tests target **Domain** + **Application** only (no Spring context); Integration tests target **Infrastructure** + **Api** (with Spring context).  
- Organize tests by **feature** and follow same package structure as production code.  
- Keep tests descriptive; use `@DisplayName`; run tests frequently (`mvn test` / `gradle test --continuous`).

**Test naming convention:**
- Unit tests: `ClassName_MethodName_ExpectedBehavior`
- Example: `Task_Complete_ShouldChangeStatusToCompleted`

**Example - Unit Test:**
```java
@DisplayName("Task completion tests")
class TaskCompletionTests {
    
    @Test
    @DisplayName("Should complete a pending task")
    void complete_WhenTaskIsPending_ShouldChangeStatusToCompleted() {
        // Arrange
        Task task = Task.create("Test task");
        
        // Act
        task.complete();
        
        // Assert
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }
    
    @Test
    @DisplayName("Should throw exception when completing already completed task")
    void complete_WhenTaskAlreadyCompleted_ShouldThrowException() {
        // Arrange
        Task task = Task.create("Test task");
        task.complete();
        
        // Act & Assert
        assertThrows(IllegalStateException.class, task::complete);
    }
}
```

**Example - Integration Test with Testcontainers:**
```java
@SpringBootTest
@Testcontainers
class TaskRepositoryIntegrationTests {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb");
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Test
    @DisplayName("Should save and retrieve task from database")
    void save_ShouldPersistTaskToDatabase() {
        // Arrange
        Task task = Task.create("Integration test task");
        
        // Act
        Task saved = taskRepository.save(task);
        Optional<Task> retrieved = taskRepository.findById(saved.getId());
        
        // Assert
        assertTrue(retrieved.isPresent());
        assertEquals(task.getTitle(), retrieved.get().getTitle());
    }
}
```

---

## 6) Spring Boot Patterns

### 6.1 REST Controllers (API Layer)
- Use `@RestController` + `@RequestMapping`
- Keep controllers thin - delegate to Application services immediately
- Use DTOs for requests/responses (never expose domain entities)
- Use Spring validation: `@Valid`, `@NotNull`, `@NotBlank`
- Return `ResponseEntity<T>` for explicit status codes
- Use `@ControllerAdvice` for global exception handling

**Example:**
```java
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public final class TaskController {
    private final TaskService taskService;
    
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request.title());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(TaskResponse.from(task));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID id) {
        return taskService.findTask(new TaskId(id))
            .map(TaskResponse::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
```

### 6.2 Repository Pattern
- Define repository interfaces in **Application** layer (ports)
- Implement with Spring Data JPA in **Infrastructure** layer (adapters)
- **NO STORED PROCEDURES** - all logic in Java
- Use method query derivation or `@Query` with JPQL
- Map between JPA entities and Domain entities in repository implementation

**Application layer (port):**
```java
public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(TaskId id);
    List<Task> findByStatus(TaskStatus status);
    void delete(TaskId id);
}
```

**Infrastructure layer (adapter):**
```java
@Repository
@RequiredArgsConstructor
public final class JpaTaskRepository implements TaskRepository {
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
}

// Spring Data JPA interface (Infrastructure)
interface SpringDataTaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByStatus(String status);
}
```

### 6.3 Service Pattern (Application Layer)
- Orchestrate use cases (commands/queries)
- Use `@Transactional` for write operations
- Keep services focused on single responsibility
- Return domain objects or Optional, not DTOs

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public final class TaskService {
    private final TaskRepository taskRepository;
    
    @Transactional
    public Task createTask(String title) {
        Task task = Task.create(title);
        return taskRepository.save(task);
    }
    
    public Optional<Task> findTask(TaskId id) {
        return taskRepository.findById(id);
    }
}
```

---

## 7) Conventional Commits
- Use `<type>([optional scope]): <description>` with 72-char subject limit.  
- Types: `feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert`.  
- Keep one logical change per commit; use scope to denote layer/feature/module.

**Examples**
```
feat(api): add task creation endpoint
fix(domain): correct task status validation
test(task): add unit tests for task completion
chore(deps): upgrade Spring Boot to 3.2.4
```

---

## 8) Object Calisthenics (lightweight)
When refactoring, prefer these constraints to keep code small and intention-revealing:
- One level of indentation per method; avoid `else` with guard clauses ("fail fast").  
- Wrap primitives into meaningful types (strongly-typed IDs, value objects).  
- Prefer **first-class collections** (wrap `List<T>` in domain collections).  
- Avoid long call chains ("Law of Demeter").  
- Don't abbreviate names; keep classes/methods small and focused.  
- No setters in domain classes; prefer factories and behavior methods.

---

## 9) Documentation Organization
- **All documentation** must be placed in the `docs/` directory at repository root
- Project README.md stays at root, but detailed docs go in `docs/`
- Documentation types and locations:
  - Architecture Decision Records (ADRs): `docs/adr/`
  - API documentation: `docs/api/`
  - User guides and tutorials: `docs/guides/`
  - Design documents: `docs/design/`
  - Lab exercises and workshop materials: `docs/labs/` or `docs/`
- Use clear, descriptive filenames: `docs/api/authentication-guide.md` not `docs/auth.md`
- Always link to docs from main README.md with relative paths
- Generate OpenAPI documentation with Springdoc

---

## 10) Practical Scaffolds & Prompts (use verbatim)

### 10.1 Generate a new Aggregate (Domain)
```
Create a DDD aggregate in [project]-domain/src/main/java/.../domain/order:
- Strongly-typed IDs (OrderId as record, CustomerId as record)
- Private constructor + static factory Order.create(...)
- Invariants: quantity > 0, price > 0
- Value object Address as immutable record { String street, String city, String country }
- Business methods: addOrderItem(), changeShippingAddress() (publish domain event)
- No navigation to other aggregates; no public setters
- Final class
```

### 10.2 REST Endpoint (API) + DI wiring
```
In [project]-api module:
- Add REST controller: GET /orders/{id}, POST /orders
- Use @RestController, @RequestMapping, ResponseEntity
- Map requests to Application service calls; no business logic in controller
- Use DTOs for request/response (CreateOrderRequest, OrderResponse)
- Configure component scanning in Spring Boot application
- Use SLF4J logger, validate inputs with @Valid
- Proper 400/404/500 handling with @ControllerAdvice and ProblemDetail
```

### 10.3 Unit test pattern (JUnit 5 + Mockito)
```
Create tests in [project]-domain/src/test/java/.../domain/order:
- One test class per domain class (TaskTests.java, TaskCompletionTests.java)
- Use JUnit 5 (@Test, @DisplayName, assertions)
- Use Mockito for collaborator interfaces (@Mock, @InjectMocks)
- Use descriptive test names and cover invalid scenarios (guard clauses)
- Follow Arrange-Act-Assert pattern
```

### 10.4 Integration test with Testcontainers
```
Create integration test in [project]-infrastructure/src/test/java:
- Use @SpringBootTest and @Testcontainers
- Set up PostgreSQL container: PostgreSQLContainer
- Test repository implementation
- Verify database persistence
- Use @DynamicPropertySource for container connection properties
```

### 10.5 Conventional commit + PR helper
```
Write a Conventional Commit subject (<=72 chars) and a PR description with:
- Intent
- Scope (layer/module/feature)
- Risk/impact
- Linked issue(s)
```

---

## 11) Guardrails (Workshop)
- Do **not** invent external dependencies without being asked.  
- Keep domain logic **out of Api/Infrastructure** layers.  
- **NO STORED PROCEDURES** - all business logic in Java.
- Prefer small, composable methods; log meaningfully with SLF4J.  
- Use Spring Boot starters; avoid unnecessary frameworks.
- If a rule conflicts, **Clean Architecture boundaries win** (then DDD, then Spring conventions).

---

## 12) Quality Standards
- **SOLID principles** are mandatory
- **70%+ test coverage** (measured with JaCoCo)
- **No primitive obsession** - use strongly-typed IDs and value objects
- **95%+ test reliability** - tests must be deterministic
- **Domain-Driven Design** - ubiquitous language in code
- **Maintainability first** - prefer readable code over clever code

---

### Appendix A — Expected Layout (Multi-Module Maven)
```
pom.xml (parent)
src-springboot/
  taskmanager-domain/
    pom.xml
    src/main/java/com/example/taskmanager/domain/
    src/test/java/com/example/taskmanager/domain/
  taskmanager-application/
    pom.xml
    src/main/java/com/example/taskmanager/application/
    src/test/java/com/example/taskmanager/application/
  taskmanager-infrastructure/
    pom.xml
    src/main/java/com/example/taskmanager/infrastructure/
    src/test/java/com/example/taskmanager/infrastructure/
  taskmanager-api/
    pom.xml
    src/main/java/com/example/taskmanager/api/
    src/test/java/com/example/taskmanager/api/
    src/main/resources/application.yml
```

---

### Appendix B — Technology Stack
- **Runtime**: Java 21 (LTS)
- **Framework**: Spring Boot 3.x
- **Build Tools**: Maven 3.9+ or Gradle 8+
- **Database**: PostgreSQL 16+ (via Spring Data JPA)
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Validation**: Jakarta Validation (Bean Validation)
- **API Docs**: Springdoc OpenAPI
- **Observability**: Spring Boot Actuator + Micrometer + OpenTelemetry
- **Logging**: SLF4J + Logback

**Key Dependencies:**
```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

### Appendix C — OpenTelemetry Observability
- Use Micrometer + OpenTelemetry for distributed tracing
- Configure Actuator endpoints for health/metrics
- Add custom metrics with `@Timed` annotations
- Use MDC (Mapped Diagnostic Context) for correlation IDs in logs
- Export traces to console for workshop demos (no complex infrastructure needed)

**Configuration (application.yml):**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  tracing:
    sampling:
      probability: 1.0
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  pattern:
    level: '%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]'
```

---

### Appendix D — Migration Patterns (Mule ESB → Spring Boot)
When migrating from Mule ESB to Spring Boot:
- **Mule Flow** → Spring Boot `@Service` or `@Component`
- **Mule Connector** → Spring `RestTemplate` or `WebClient`
- **DataWeave Transform** → Java record mapping or MapStruct
- **Mule Error Handling** → Spring `@ControllerAdvice` / `@ExceptionHandler`
- **Mule Properties** → Spring `@ConfigurationProperties` or `application.yml`
- **Mule API Gateway** → Spring Cloud Gateway or dedicated API Gateway (Kong)
- **Mule Batch** → Spring Batch
- **Mule VM Queue** → Spring `@Async` or message broker (RabbitMQ/Kafka)

**Key Principle**: Extract business logic to domain layer first, then map integration patterns.
