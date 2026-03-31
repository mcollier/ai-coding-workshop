---
name: test-data-generator
description: Generates realistic test data for .NET integration tests, including entities, DTOs, and database seed data. Useful for creating consistent test fixtures and mocking external data sources.
argument-hint: "[entity type] [count] [optional: format]"
user-invocable: true
disable-model-invocation: false
---

# Test Data Generator Skill

This skill helps generate realistic, consistent test data for .NET integration tests. It can create entities, DTOs, database seed data, and mock API responses.

## Purpose

Integration tests require realistic data, but manually creating test fixtures is time-consuming and error-prone. This skill provides:

- **Consistent test data** across test suites
- **Realistic values** appropriate for entity types
- **Varied scenarios** including edge cases
- **Multiple format options** (C# objects, JSON, SQL)

## When to Use This Skill

✅ **Use this skill when:**
- Writing integration tests that need database seed data
- Creating test fixtures for API endpoint testing
- Generating mock responses for external services
- Need varied test scenarios (happy path, edge cases, invalid data)

❌ **Don't use this skill for:**
- Unit tests with simple test doubles (use FakeItEasy directly)
- Production data generation (use proper seed scripts)
- One-off test data (manual creation is faster)

## Usage

### Slash Command Invocation

```
/test-data-generator User 10
/test-data-generator Order 5 json
/test-data-generator Task 3 seeder
```

### Automatic Invocation

Simply mention your need in chat:
- "I need test data for User integration tests"
- "Generate sample orders for API testing"
- "Create test tasks with various due dates"

## Instructions

When invoked, this skill generates test data following these steps:

### Step 1: Understand the Entity

- Identify the entity type (User, Order, Task, etc.)
- Determine required properties based on entity definition
- Consider relationships to other entities

### Step 2: Generate Realistic Values

**For common types:**
- **Names**: Use realistic first/last names (diverse representation)
- **Emails**: Generate valid email formats (user@domain.com)
- **Dates**: Vary dates appropriately for context
  - CreatedDate: Recent past dates
  - DueDate: Future dates for tasks/orders
  - BirthDate: Adult ages for users (18-80 years)
- **IDs**: Use sequential or GUID format depending on entity
- **Status/Enums**: Include all enum values across dataset
- **Numeric values**: Realistic ranges (prices 10-1000, quantities 1-100)

**For .NET/C# specific:**
- Use `DateTimeOffset.UtcNow` for timestamps
- Generate `Guid.NewGuid()` for GUIDs
- Follow .NET naming conventions (PascalCase properties)

### Step 3: Include Edge Cases

For robust testing, include:
- Minimum/maximum values
- Empty/null optional fields
- Boundary conditions
- Invalid but realistic data (for negative tests)

### Step 4: Choose Output Format

**C# Object Initialization:**
```csharp
public static class TestData
{
    public static User[] Users => new[]
    {
        new User { Id = Guid.Parse("..."), Name = "...", ... },
        // ...
    };
}
```

**JSON Format:**
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Jane Doe",
    ...
  }
]
```

**Entity Framework Seeder:**
```csharp
modelBuilder.Entity<User>().HasData(
    new User { Id = 1, Name = "...", ... },
    // ...
);
```

### Step 5: Add Context Comments

Include comments explaining:
- Why specific values were chosen
- Which scenarios each item represents
- How to extend the dataset

## Examples

### Example 1: Generate Task Test Data

**Request:**
```
/test-data-generator Task 5
```

**Output:**
```csharp
public static class TaskTestData
{
    public static TaskEntity[] SampleTasks => new[]
    {
        // Happy path: Standard task with all fields
        new TaskEntity
        {
            Id = Guid.Parse("11111111-1111-1111-1111-111111111111"),
            Title = "Complete project documentation",
            Description = "Write comprehensive docs for TaskManager API",
            Status = TaskStatus.InProgress,
            Priority = Priority.High,
            DueDate = DateTimeOffset.UtcNow.AddDays(7),
            CreatedDate = DateTimeOffset.UtcNow.AddDays(-2),
            AssignedTo = "user@example.com"
        },
        
        // Edge case: Task with minimum required fields
        new TaskEntity
        {
            Id = Guid.Parse("22222222-2222-2222-2222-222222222222"),
            Title = "Quick task",
            Status = TaskStatus.NotStarted,
            CreatedDate = DateTimeOffset.UtcNow
        },
        
        // Edge case: Overdue task
        new TaskEntity
        {
            Id = Guid.Parse("33333333-3333-3333-3333-333333333333"),
            Title = "Overdue bugfix",
            Status = TaskStatus.InProgress,
            Priority = Priority.Critical,
            DueDate = DateTimeOffset.UtcNow.AddDays(-5),
            CreatedDate = DateTimeOffset.UtcNow.AddDays(-10)
        },
        
        // Edge case: Completed task
        new TaskEntity
        {
            Id = Guid.Parse("44444444-4444-4444-4444-444444444444"),
            Title = "Implement unit tests",
            Status = TaskStatus.Completed,
            Priority = Priority.Medium,
            DueDate = DateTimeOffset.UtcNow.AddDays(-1),
            CreatedDate = DateTimeOffset.UtcNow.AddDays(-14),
            CompletedDate = DateTimeOffset.UtcNow.AddDays(-2)
        },
        
        // Edge case: Low priority task far in future
        new TaskEntity
        {
            Id = Guid.Parse("55555555-5555-5555-5555-555555555555"),
            Title = "Research new technologies",
            Description = "Investigate emerging patterns for Q2",
            Status = TaskStatus.NotStarted,
            Priority = Priority.Low,
            DueDate = DateTimeOffset.UtcNow.AddDays(60),
            CreatedDate = DateTimeOffset.UtcNow
        }
    };
}
```

### Example 2: Generate JSON Test Data

**Request:**
```
/test-data-generator User 3 json
```

**Output:**
```json
[
  {
    "id": "10000000-0000-0000-0000-000000000001",
    "email": "john.smith@example.com",
    "firstName": "John",
    "lastName": "Smith",
    "role": "Developer",
    "isActive": true,
    "createdDate": "2024-03-15T10:30:00Z"
  },
  {
    "id": "10000000-0000-0000-0000-000000000002",
    "email": "maria.garcia@example.com",
    "firstName": "Maria",
    "lastName": "Garcia",
    "role": "Manager",
    "isActive": true,
    "createdDate": "2024-02-20T14:15:00Z"
  },
  {
    "id": "10000000-0000-0000-0000-000000000003",
    "email": "inactive.user@example.com",
    "firstName": "Inactive",
    "lastName": "User",
    "role": "Developer",
    "isActive": false,
    "createdDate": "2023-12-01T09:00:00Z"
  }
]
```

## Templates

Reference the [template.cs](./template.cs) file in this skill directory for a reusable test data class structure.

## Best Practices

### Data Variety
- Include diverse names, emails, and scenarios
- Represent different enum/status values
- Mix required and optional fields

### Maintainability
- Use constants for magic numbers
- Group related test data
- Add descriptive comments
- Use meaningful IDs (not just sequential 1,2,3)

### Integration with xUnit
```csharp
public class TaskIntegrationTests : IClassFixture<WebApplicationFactory<Program>>
{
    [Theory]
    [MemberData(nameof(TaskTestData.SampleTasks))]
    public async Task CreateTask_ValidData_ReturnsCreated(TaskEntity task)
    {
        // Test using generated data
    }
}
```

## Extending This Skill

To customize for your project:
1. Update entity types in examples
2. Add project-specific validation rules
3. Include custom value objects (Priority, Status, etc.)
4. Reference project domain models

## Related Resources

- [FakeItEasy](https://fakeiteasy.github.io/) - For mocking dependencies
- [xUnit Data Attributes](https://xunit.net/docs/shared-context) - For test data strategies
- [Bogus](https://github.com/bchavez/Bogus) - .NET library for fake data generation (if you want to automate)

---

**Note:** This skill provides guidelines and examples. For large-scale test data generation in production scenarios, consider using specialized libraries like Bogus or AutoFixture.
