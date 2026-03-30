---
marp: true
theme: default
paginate: true
backgroundColor: #fff
---

# GitHub Copilot for .NET Development

## AI-Assisted Development Fundamentals

**Duration:** 3 Hours  
**Format:** Instructor-led, hands-on  
**Part:** 1 of 2

---

# Welcome to AI-Assisted Development

## What You'll Learn Today

- Use **Copilot Instructions** for team-wide consistency
- Apply **Test-Driven Development** with AI assistance
- Transform **requirements into working code**
- Generate and **refactor APIs** following Clean Architecture
- Create **comprehensive tests and documentation**
- Follow **conventional commits** and AI-generated PR descriptions

---

# Why This Workshop Matters

**Traditional Development:**
- Manual test writing
- Repetitive CRUD code
- Documentation debt
- Inconsistent patterns across team

**With GitHub Copilot:**
- AI generates tests from specifications
- Accelerated implementation
- Documentation alongside code
- Encoded team standards via Copilot Instructions

---

# Prerequisites Check

✅ **GitHub Copilot** - Active subscription  
✅ **.NET 9 SDK** - `dotnet --version`  
✅ **VS Code** - With C# Dev Kit extension  
✅ **Git** - Basic familiarity  
✅ **C# Experience** - Comfortable with syntax

**Environment Check:**
```bash
dotnet --version    # Should show 9.x.x
git --version
code --version
```

---

# Repository Structure

```
TaskManager.sln
├── src/
│   ├── TaskManager.Domain/        # Business logic
│   ├── TaskManager.Application/   # Use cases
│   ├── TaskManager.Infrastructure/ # Data access
│   └── TaskManager.Api/           # Minimal API
└── tests/
    ├── TaskManager.UnitTests/
    └── TaskManager.IntegrationTests/
```

**Architecture:** Clean Architecture + DDD  
**Testing:** xUnit + FakeItEasy  
**API:** .NET 9 Minimal APIs

---

# Today's Journey

```
0. Kickoff & Setup (15 min)
0.5. Copilot Features Tour (15 min)
1. Copilot Instructions & TDD (30 min)
2. Requirements → Code (45 min)
3. Code Generation & Refactoring (45 min)
4. Testing & Documentation (15 min)
5. Wrap-Up & Discussion (15 min)
```

**Total:** 3 hours with hands-on labs

---

<!-- _class: lead -->

# Module 0

## Setup & Environment
### Getting Ready

**Duration:** 15 minutes

---

# Clone and Branch

```bash
git clone https://github.com/centricconsulting/ai-coding-workshop.git
cd ai-coding-workshop

# Create your personal branch
git checkout main
git pull
git checkout -b your-name-workshop
```

**Important:** Work on your own branch to avoid conflicts

---

# Verify Environment

```bash
# Check .NET
dotnet --version

# Build solution
dotnet build

# Run tests
dotnet test
```

**Expected:** All should succeed

---

# Copilot Instructions Preview

This repository includes **`.github/copilot-instructions.md`**

**What it does:**
- Automatically applied to all Copilot interactions
- Encodes Clean Architecture rules
- Enforces DDD patterns
- Specifies .NET 9 conventions

**No setup needed** - It just works!

---

<!-- _class: lead -->

# Module 0.5

## GitHub Copilot Features Tour
### Capabilities Overview

**Duration:** 15 minutes

---

# Three Ways to Interact

## 1. Inline Completions
- Active while typing
- Suggests next lines
- **Trigger:** Start typing or use `Alt+\` (Windows/Linux) or `Option+\` (Mac)

## 2. Chat Panel
- Ask questions, explain code
- Get suggestions without editing
- **Trigger:** Click Copilot icon in sidebar

## 3. Inline Chat
- Edit code in-place
- **Trigger:** `Ctrl+I` (Windows/Linux) or `Cmd+I` (Mac)

---

# Slash Commands

Quick shortcuts for common tasks:

| Command | Purpose | Example |
|---------|---------|---------|
| `/explain` | Explain code | `/explain this function` |
| `/fix` | Suggest fixes | `/fix this error` |
| `/tests` | Generate tests | `/tests for this class` |
| `/doc` | Add documentation | `/doc this API` |
| `/refactor` | Improve code | `/refactor to use guard clauses` |

---

# More Slash Commands

| Command | Purpose | Example |
|---------|---------|---------|
| `/help` | Show all commands | `/help` |
| `/agents` | List custom agents | `/agents` |
| `/skills` | List skills | `/skills` |
| `/init` | Start new project | `/init dotnet webapi` |
| `/create-file` | Create with AI | `/create-file readme.md` |

**Try it now:** Type `/help` in Copilot Chat

---

# Chat Participants

Provide context for better results:

## @workspace
- Questions about your codebase
- "Where is the Task entity?"

## @vscode
- VS Code features and commands
- "How do I debug tests?"

## @terminal
- CLI commands and troubleshooting
- "How to restore packages?"

---

# Context Variables

Reference specific context:

## #file
- Reference a specific file
- Example: `Explain #file:Task.cs`

## #selection
- Current editor selection
- Example: `Refactor #selection`

## #editor
- Current active file
- Example: `Add tests for #editor`

---

# Quick Practice (5 min)

**Try these:**

1. Open `TaskManager.Domain/Tasks/Task.cs`
2. Use `/explain` on the Task class
3. Try `@workspace` with: "What's the architecture pattern?"
4. Use `#file` with: "Suggest improvements for #file:Task.cs"

**Goal:** Get comfortable with Copilot's features

---

<!-- _class: lead -->

# Module 1

## Copilot Instructions & TDD
### Red-Green-Refactor with AI

**Duration:** 30 minutes

---

# What Are Copilot Instructions?

**Location:** `.github/copilot-instructions.md`

**Purpose:**
- Repository-wide AI behavior
- Always active (no manual activation)
- Team standards enforcement
- Consistent across all team members

**Think of it as:** A senior developer reviewing every suggestion

---

# Our Copilot Instructions

Key rules encoded:

- ✅ **TDD first** - Write tests before implementation
- ✅ **Clean Architecture** - Domain has no dependencies
- ✅ **DDD patterns** - Aggregates, Value Objects, Entities
- ✅ **.NET 9** - Minimal APIs, file-scoped namespaces
- ✅ **Testing** - xUnit + FakeItEasy
- ✅ **Naming** - PascalCase for types, camelCase for variables

---

# Test-Driven Development (TDD)

```
🔴 RED → ✅ GREEN → ♻️ REFACTOR
```

## Red Phase
Write a **failing test** that defines desired behavior

## Green Phase
Write **minimal code** to make the test pass

## Refactor Phase
**Improve code quality** while keeping tests green

---

# Why TDD with Copilot?

**Traditional concern:** "AI writes code without tests"

**Our approach:**
- Copilot Instructions **enforce tests first**
- AI generates test cases from requirements
- Tests are easier to review than implementations
- Validates AI-generated code immediately

**Result:** Higher quality, verified code

---

# Lab 1 Overview

**Build:** NotificationService with TDD

**Steps:**
1. Define `INotificationService` interface (RED)
2. Generate comprehensive test suite (RED)
3. Implement `NotificationService` (GREEN)
4. Refactor for quality (REFACTOR)

**Time:** 25 minutes

**Key learning:** AI accelerates TDD, doesn't bypass it

---

# Demo: TDD Workflow

**Watch for:**
- How Copilot suggests test scenarios
- Interface-first design
- Test structure (Arrange-Act-Assert)
- Implementation simplicity
- Refactoring suggestions

**Then:** You'll do it hands-on in Lab 1

---

<!-- _class: lead -->

# Module 2

## Requirements → Backlog → Code
### Transforming User Stories

**Duration:** 45 minutes

---

# The Challenge

**What you get:**
> "As a user, I want to prioritize my tasks"

**What you need:**
- Acceptance criteria
- Domain model changes
- Application logic
- API endpoints
- Comprehensive tests

**With Copilot:** Generate all of this systematically

---

# Our Approach

```
User Story
  ↓
Backlog Item with Acceptance Criteria (Copilot)
  ↓
Domain Model (TDD: Tests first)
  ↓
Application Layer (Commands/Queries)
  ↓
API Endpoint (Minimal API)
  ↓
Integration Tests
```

---

# Lab 2 Overview

**Build:** Task Priority feature

**What you'll create:**
- `Priority` value object (Low, Medium, High)
- `Task` entity with Priority property
- `CreateTaskCommand` with handler
- `POST /tasks` API endpoint
- Full test coverage

**Architecture layers:** Domain → Application → API

---

# Domain-Driven Design Patterns

## Value Objects
- Immutable
- Value equality
- Example: `Priority`, `Address`

## Entities
- Identity-based equality
- Lifecycles
- Example: `Task`, `User`

## Aggregates
- Consistency boundaries
- Example: `Task` (aggregate root)

**Copilot knows these patterns** via Instructions

---

# Key Technique: Context Variables

```
Instead of:
"Create a task entity"

Use:
"Add Priority property to #file:Task.cs following DDD patterns"
```

**Why:** More precise, leverages existing context

---

<!-- _class: lead -->

# Module 3

## Code Generation & Refactoring
### Scaffolding and Modernization

**Duration:** 45 minutes

---

# Two Workflows

## 1. Generate from Scratch
- Complete CRUD endpoints
- Following Clean Architecture
- With query handlers (CQRS)

## 2. Refactor Legacy Code
- Modernize old patterns
- Apply Object Calisthenics
- Improve testability

---

# Lab 3: Part A - Generation

**Generate:**
- `GET /tasks/{id}` - Retrieve single task
- `PUT /tasks/{id}` - Update task
- `DELETE /tasks/{id}` - Delete task

**Using:**
- `@workspace` for context
- Minimal API patterns
- CQRS queries
- Integration tests

**Time:** 20 minutes

---

# CQRS Pattern

**Command-Query Responsibility Segregation**

## Commands
- Change state
- Example: `CreateTaskCommand`, `UpdateTaskCommand`

## Queries
- Read state
- Example: `GetTaskByIdQuery`, `GetAllTasksQuery`

**Benefit:** Clear separation of read/write concerns

---

# Lab 3: Part B - Refactoring

**Legacy code:** `LegacyTaskProcessor.cs`

**Problems:**
- Nested if statements
- Abbreviations
- No guard clauses
- Poor testability

**Your task:** Use `/refactor` to modernize

**Principles:** Object Calisthenics

---

# Object Calisthenics (Light)

**Key rules:**
- One level of indentation per method
- Don't use `else` - use guard clauses
- No abbreviations
- Wrap primitives in meaningful types
- Small methods with clear names

**Example:**
```csharp
// ❌ Before
if (task != null) {
    if (task.Status == "Active") {
        // logic
    }
}

// ✅ After
if (task == null) return;
if (task.Status != "Active") return;
// logic
```

---

# Using @workspace

**Why it matters:**
- Copilot sees your entire codebase
- Suggests patterns already in use
- Maintains consistency

**Example prompts:**
- `@workspace Show me all domain entities`
- `@workspace How are minimal APIs structured in this project?`
- `@workspace Generate GET endpoint following existing pattern`

---

<!-- _class: lead -->

# Module 4

## Testing, Documentation & Workflow
### Completing the Lifecycle

**Duration:** 15 minutes

---

# Lab 4 Overview

**Complete the development lifecycle:**

1. Generate comprehensive tests (`/tests`)
2. Add XML documentation (`/doc`)
3. Update API documentation
4. Write conventional commit messages
5. Generate PR description

**Time:** 15 minutes  
**Goal:** AI-assisted workflow end-to-end

---

# Generate Tests with /tests

```
# In Copilot Chat
/tests for CreateTaskCommand
```

**Copilot generates:**
- Happy path tests
- Edge cases
- Boundary conditions
- Error scenarios

**Your job:** Review, adjust, run

---

# Documentation with /doc

```
# In Copilot Chat
/doc for ITaskService
```

**Generates:**
- XML documentation comments
- Parameter descriptions
- Return value docs
- Example usage

**Result:** IntelliSense-ready documentation

---

# Conventional Commits

**Format:** `<type>(<scope>): <description>`

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation
- `test` - Tests
- `refactor` - Code restructuring
- `chore` - Maintenance

**Example:** `feat(domain): add Priority value object`

---

# PR Description with @workspace

```
@workspace Generate a PR description for my changes
```

**Copilot provides:**
- Summary of changes
- Files modified
- Testing notes
- Breaking changes (if any)

**Then:** Review and adjust before creating PR

---

<!-- _class: lead -->

# Module 5

## Wrap-Up & Discussion
### Lessons Learned

**Duration:** 15 minutes

---

# What We Covered

✅ **Copilot Instructions** - Team-wide consistency  
✅ **TDD with AI** - Tests first, always  
✅ **Requirements → Code** - Systematic transformation  
✅ **Clean Architecture** - Maintained via AI  
✅ **Refactoring** - Modernize legacy code  
✅ **Full lifecycle** - Tests, docs, commits, PRs

---

# Key Takeaways

## 1. AI Amplifies Good Practices
- TDD becomes faster
- Architecture patterns enforced
- Documentation debt reduced

## 2. Context Matters
- Use `@workspace`, `#file`, `#selection`
- Copilot Instructions encode team knowledge
- Better prompts = better results

## 3. Human Accountability
- Review all AI suggestions
- Tests validate correctness
- You own the code

---

# Common Pitfalls to Avoid

❌ **Accepting suggestions blindly**  
✅ Review, understand, test

❌ **Skipping tests to go faster**  
✅ Tests first catches errors early

❌ **Generic prompts**  
✅ Use context variables and be specific

❌ **Ignoring architecture**  
✅ Copilot Instructions enforce patterns

---

# Anti-Patterns

## Over-reliance
- AI is a tool, not a replacement for thinking
- Understand what code does

## Under-leveraging
- Use Copilot for repetitive tasks
- Don't type boilerplate manually

## Inconsistent standards
- Use Copilot Instructions
- Encode team decisions

---

# Best Practices

✅ **Start with plan** - Ask Copilot for approach first  
✅ **Tests first** - TDD even with AI  
✅ **Review iteratively** - Don't wait until the end  
✅ **Use chat participants** - @workspace is powerful  
✅ **Leverage instructions** - Team knowledge encoded  
✅ **Commit frequently** - Small, focused changes

---

# Discussion Questions

1. **What surprised you most** about AI-assisted development?
2. **Where did Copilot excel?** Where did it struggle?
3. **How would you use this** in your daily work?
4. **What team standards** should you encode in instructions?
5. **What concerns** do you still have?

---

# Next Steps: Part 2

**Advanced GitHub Copilot (Part 2):**
- Interaction models (Ask, Edit, Agent)
- Skills & Customization Hierarchy
- Custom Copilot Agents
- Agent design and handoffs
- Build your own production agent

**When:** [Scheduled time]  
**Duration:** 3 hours

---

# Additional Practice

**Reference Implementation:**
- Branch: `test-lab-walkthrough`
- All labs completed
- Best practices demonstrated

**Use it to:**
- Compare your solutions
- See patterns in action
- Continue learning

```bash
git checkout test-lab-walkthrough
```

---

# Resources

📚 **Lab Guides:** `docs/labs/`  
📝 **Copilot Instructions:** `.github/copilot-instructions.md`  
👥 **Facilitator Guide:** `docs/FACILITATOR_GUIDE.md`  
🎨 **Architecture:** `docs/design/architecture.md`

**GitHub Copilot Docs:**  
https://docs.github.com/copilot

---

# Immediate Actions

**This week:**
1. Try Copilot for **one TDD task**
2. Create **Copilot Instructions** for your repo
3. Use `/tests` and `/doc` in daily work

**Next month:**
1. Encode **team standards** in instructions
2. Measure **velocity improvement**
3. Share **learnings with team**

---

<!-- _class: lead -->

# Thank You!

## Questions?

**Feedback and contributions:**  
[CONTRIBUTING.md](../../CONTRIBUTING.md)

**See you in Part 2:**  
Advanced GitHub Copilot with Custom Agents

---

# Remember

> AI is a force multiplier  
> Good practices + Copilot = Great results  
> You are accountable for the code

**Keep learning, keep improving!**
