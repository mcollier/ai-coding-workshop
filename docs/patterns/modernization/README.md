# Modernization Pattern Library

Documented patterns for migrating **Mule ESB applications to Spring Boot microservices**.

## Purpose

This pattern library provides:
- ✅ **Before/After examples** for common Mule ESB patterns
- ✅ **Step-by-step migration guidance** from legacy to modern implementations
- ✅ **Testing strategies** for validating migrated code
- ✅ **Common pitfalls** and how to avoid them
- ✅ **Related patterns** for complete modernization workflows

## Target Audience

- **Development Teams** migrating Mule ESB applications to Spring Boot
- **Technical Leaders** planning modernization initiatives
- **AI-Assisted Developers** using the Modernization agent
- **Enterprise Java Workshop Participants** (Labs 3, 8, 9)

## Pattern Template

Each pattern follows a standardized 8-section structure:

1. **Overview** - Pattern name and purpose
2. **Context** - When to use this pattern  
3. **Before** - Legacy code example (Mule ESB XML/DataWeave)
4. **After** - Modern code example (Spring Boot Java)
5. **Migration Steps** - Step-by-step transformation guide
6. **Testing** - How to test the migrated code  
7. **Gotchas** - Common pitfalls and avoidance strategies
8. **Related Patterns** - Links to related transformation patterns

---

## Pattern Categories

### 1. Flow Transformation Patterns

**[Mule Flow → Spring Boot Service](./01-flow-transformation.md)**  
Transform Mule XML flows into Spring Boot service classes with Clean Architecture layering.

- Example: Order processing flow → OrderService
- Covers: Flow structure, component mapping, dependency injection
- Testing: JUnit 5 + Mockito service tests

### 2. Data Access Patterns

**[Stored Procedures → Repository Pattern](./02-data-access-stored-procedures.md)**  
Eliminate stored procedures in favor of Spring Data JPA repositories with domain-driven design.

- Example: `sp_GetOrdersByCustomer` → `OrderRepository.findByCustomerId()`
- Covers: Entity mapping, query methods, complex queries (JPQL)
- Testing: Testcontainers with PostgreSQL

**[JDBC → Spring Data JPA](./02-data-access-jdbc.md)**  
Migrate raw JDBC connections to Spring Data JPA with entity management.

- Example: Manual JDBC queries → JPA entities
- Covers: Connection management, transaction boundaries, query derivation
- Testing: In-memory H2 for unit tests, Testcontainers for integration tests

### 3. Integration Patterns

**[HTTP Request → RestTemplate/WebClient](./03-integration-http.md)**  
Migrate Mule HTTP connectors to Spring's REST client capabilities.

- Example: Mule HTTP request → WebClient reactive calls
- Covers: Request/response mapping, error handling, circuit breakers
- Testing: WireMock for HTTP mocking

**[Message Queue → Spring AMQP/Kafka](./03-integration-messaging.md)**  
Transform Mule JMS/AMQP connectors to Spring messaging abstractions.

- Example: Mule JMS listener → @RabbitListener
- Covers: Message serialization, transaction management, dead-letter queues
- Testing: Testcontainers with RabbitMQ/Kafka

### 4. Transformation Patterns

**[DataWeave → Java Business Logic](./04-transformation-dataweave.md)**  
Convert DataWeave transformations into type-safe Java code with mapping frameworks.

- Example: DataWeave JSON transformation → MapStruct mapper
- Covers: Object mapping, null handling, nested structures
- Testing: Parameterized tests for transformation logic

### 5. Error Handling Patterns

**[Mule Error Handling → Spring Exception Handling](./05-error-handling.md)**  
Standardize error responses using Spring's exception handling mechanisms.

- Example: Mule error handlers → @ControllerAdvice + domain exceptions
- Covers: Exception hierarchy, global error handlers, RFC 7807 Problem Details
- Testing: MockMvc for controller exception scenarios

### 6. Configuration Patterns

**[Mule Properties → Spring Configuration](./06-configuration.md)**  
Migrate Mule configuration files to Spring Boot's externalized configuration.

- Example: Mule `config-{env}.yaml` → Spring `application-{profile}.yml`
- Covers: Property sources, profile activation, environment overrides
- Testing: @TestPropertySource for test configurations

### 7. Observability Patterns

**[Adding OpenTelemetry](./07-observability.md)**  
Implement distributed tracing, metrics, and structured logging for microservices.

- Example: Mule monitoring → OpenTelemetry + Micrometer + SLF4J
- Covers: Trace context propagation, custom metrics, log correlation
- Testing: Trace verification in integration tests

---

## Pattern Usage Guide

### For Individual Developers

1. **Identify the pattern category** matching your Mule component
2. **Read the Before section** to confirm alignment with your legacy code
3. **Follow the Migration Steps** sequentially
4. **Use the Testing section** to validate your implementation
5. **Check Gotchas** to avoid common mistakes

### For AI-Assisted Development

Use the **Modernization agent** (`@modernization`) to:
- Extract requirements from Mule flow XML
- Generate Spring Boot implementation stubs
- Produce Mermaid diagrams for documentation
- Link to relevant patterns automatically

Example:
```
@modernization analyze this Mule flow and extract requirements: 
[paste Mule XML]
```

The agent will:
1. Parse the Mule flow structure
2. Identify applicable patterns from this library
3. Generate requirements document with Spring Boot mappings
4. Produce sequence diagrams and ER diagrams

### For Teams

**Pattern-Driven Sprints:**
1. Group Mule flows by pattern category
2. Assign sprints by pattern (e.g., "Sprint 3: Data Access Migration")
3. Use patterns as **Definition of Done** checklist
4. Share team learnings as pattern enhancements

**Pattern Review Process:**
1. Code reviews reference specific patterns
2. Use Architecture Reviewer agent to validate pattern adherence
3. Document deviations in pattern "Gotchas" section

---

## Pattern Maturity Legend

Each pattern includes a maturity indicator:

- 🟢 **Production-Ready** - Validated in multiple projects, comprehensive examples
- 🟡 **Emerging** - Validated in 1-2 projects, may need adaptation
- 🔵 **Experimental** - Conceptual, requires validation in your context

*All patterns in version 1.0 are marked Production-Ready based on Centric Consulting enterprise migration experience.*

---

## Contributing to Patterns

Found a better approach? Discovered a new gotcha? **Contributions welcome!**

### Pattern Enhancement Process

1. **Fork the repository**
2. **Update or add a pattern** following the 8-section template
3. **Include real-world code examples** (sanitize sensitive data)
4. **Add testing section** with working test code
5. **Submit pull request** with rationale

### New Pattern Submission

If you discover a Mule pattern not covered here:
1. Create a new file in the appropriate category directory
2. Follow the 8-section template strictly
3. Link from this README.md
4. Provide at least one complete before/after example

---

## Related Resources

- **Modernization Agent:** [.github/agents/modernization.agent.md](../../.github/agents/modernization.agent.md)
- **Spring Boot Instructions:** [.github/instructions/springboot.instructions.md](../../.github/instructions/springboot.instructions.md)
- **Pattern Translation Guide:** [docs/guides/dotnet-to-springboot-patterns.md](../guides/dotnet-to-springboot-patterns.md)
- **Enterprise Java Context:** [docs/enterprise-java-context.md](../enterprise-java-context.md)
- **Workshop Labs:**
  - Lab 3: Generation and Refactoring ([lab-03-generation-and-refactoring-java.md](../labs/lab-03-generation-and-refactoring-java.md))
  - Lab 8: Workflow Agents ([lab-08-workflow-agents.md](../labs/lab-08-workflow-agents.md))
  - Lab 9: Agent Design ([lab-09-agent-design.md](../labs/lab-09-agent-design.md))

---

**Version:** 1.0  
**Last Updated:** April 2, 2026  
**Maintainer:** Centric Consulting  
**Epic:** #16 - Spring Boot Workshop Adaptation  
**Issue:** #29
