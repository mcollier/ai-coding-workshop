# Test Data Generator Skill

This skill helps generate realistic test data for .NET integration tests.

## What This Skill Does

- Generates C# test data objects with realistic values
- Creates JSON test data for API testing
- Produces Entity Framework seed data
- Includes edge cases and boundary conditions
- Provides consistent test fixtures across your test suite

## How to Use

### In VS Code with Copilot

**Option 1: Slash Command**
```
/test-data-generator User 10
/test-data-generator Order 5 json
/test-data-generator Task 3 seeder
```

**Option 2: Natural Language**
Simply mention your need in Copilot Chat:
- "I need test data for User integration tests"
- "Generate 5 sample tasks with various states"
- "Create test orders in JSON format"

Copilot will automatically load this skill when relevant.

## Skill Structure

```
test-data-generator/
├── SKILL.md       # Main skill instructions (this file defines the skill)
├── template.cs    # Reusable template for test data classes
└── README.md      # Documentation (you're reading it!)
```

## What Makes This a Skill?

Skills are **portable capabilities** that:
- Work across VS Code, GitHub Copilot CLI, and cloud agents
- Include instructions + resources (like the template.cs file)
- Load automatically when relevant to your task
- Can be invoked manually as slash commands

**Compare to:**
- **Custom Agent**: Would be a "Test Data Specialist" persona with tool restrictions
- **Custom Instructions**: Would apply test data rules to ALL code generation (too broad)
- **Prompt File**: Would be a one-time "generate test data" task (not reusable)

## Examples

See [SKILL.md](./SKILL.md) for detailed examples including:
- C# object initialization
- JSON format output
- Entity Framework seeder format
- Edge case scenarios

## Customizing for Your Project

1. Fork this skill to your project's `.github/skills/` directory
2. Update entity types to match your domain
3. Add project-specific validation rules
4. Extend examples with your value objects

## Related Skills

- `integration-test-helper` - Full integration test setup
- `api-test-builder` - API endpoint test generation
- `mock-data-generator` - External service mock responses

---

**Tip:** You can create new skills with `/create-skill` in Copilot Chat!
