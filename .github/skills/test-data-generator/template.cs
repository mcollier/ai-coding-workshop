// Test Data Template
// Use this as a starting point for generating test data in your integration tests

namespace TaskManager.IntegrationTests.TestData;

/// <summary>
/// Provides reusable test data for integration tests.
/// Generated data includes happy path, edge cases, and boundary conditions.
/// </summary>
public static class TestDataTemplate<TEntity> where TEntity : class
{
    /// <summary>
    /// Sample entities for testing various scenarios
    /// </summary>
    public static TEntity[] SampleEntities => GenerateTestData();

    private static TEntity[] GenerateTestData()
    {
        // TODO: Generate entities based on TEntity type
        // Include:
        // 1. Happy path: Standard valid entity with all required fields
        // 2. Minimal: Entity with only required fields
        // 3. Maximal: Entity with all optional fields populated
        // 4. Edge cases: Boundary values, empty strings, nulls
        // 5. Invalid: Data that should fail validation
        
        return Array.Empty<TEntity>();
    }

    /// <summary>
    /// Valid entities for positive test cases
    /// </summary>
    public static IEnumerable<TEntity> ValidEntities()
    {
        // Return entities that should pass validation
        yield break;
    }

    /// <summary>
    /// Invalid entities for negative test cases
    /// </summary>
    public static IEnumerable<TEntity> InvalidEntities()
    {
        // Return entities that should fail validation
        yield break;
    }

    /// <summary>
    /// Edge case entities for boundary testing
    /// </summary>
    public static IEnumerable<TEntity> EdgeCases()
    {
        // Return entities at boundary conditions
        yield break;
    }
}

// Example Usage:
/*
public class TaskTestData
{
    public static TaskEntity[] SampleTasks => new[]
    {
        new TaskEntity 
        { 
            Id = Guid.Parse("11111111-1111-1111-1111-111111111111"),
            Title = "Sample task",
            Status = TaskStatus.NotStarted,
            CreatedDate = DateTimeOffset.UtcNow
        },
        // Add more test cases...
    };
}

// In your test:
[Theory]
[MemberData(nameof(TaskTestData.SampleTasks))]
public async Task TestMethod(TaskEntity task)
{
    // Test implementation
}
*/
