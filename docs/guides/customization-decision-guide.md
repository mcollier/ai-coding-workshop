# Copilot Customization Decision Guide

**Version:** 1.0  
**Last Updated:** March 30, 2026  
**Audience:** Developers, architects, team leads

---

## Overview

GitHub Copilot offers **four main customization approaches** to tailor AI behavior for your team's needs. This guide helps you choose the right approach for your specific use case.

## The Four Customization Types

### 1. Custom Instructions (`.instructions.md`)
**What:** Always-on rules that apply to all code generation  
**When Applied:** Continuously, in the background  
**Best For:** Coding standards, conventions, and guardrails

### 2. Agent Skills (`SKILL.md`)
**What:** Portable capabilities with instructions, scripts, and resources  
**When Applied:** On-demand (slash command or automatic loading)  
**Best For:** Specialized tasks requiring multi-step procedures

### 3. Custom Agents (`.agent.md`)
**What:** Role-based personas with tool restrictions and handoffs  
**When Applied:** When explicitly selected from agent dropdown  
**Best For:** Workflow orchestration and role-specific behaviors

### 4. Prompt Files (`.prompt.md`)
**What:** Quick one-off task automation  
**When Applied:** On-demand as slash commands  
**Best For:** Repeated simple tasks without complexity

---

## Decision Tree

```
START: What do you need to customize?

├─ Need to ENFORCE coding standards across ALL files?
│  └─> Use CUSTOM INSTRUCTIONS
│      Examples: "Always use sealed classes", "Follow Clean Architecture"
│      Location: .github/instructions/*.instructions.md
│
├─ Need a SPECIALIZED CAPABILITY with scripts/resources?
│  └─> Use AGENT SKILL
│      Examples: Database migration workflow, Test data generation
│      Location: .github/skills/skill-name/SKILL.md
│      ✓ Portable (works in VS Code, CLI, cloud)
│
├─ Need a ROLE-BASED ASSISTANT with tool restrictions?
│  └─> Use CUSTOM AGENT
│      Examples: Architecture reviewer (read-only), Security auditor
│      Location: .github/agents/agent-name.agent.md
│      ✓ Can restrict tools, define handoffs
│
└─ Need QUICK AUTOMATION for a simple recurring task?
   └─> Use PROMPT FILE
       Examples: Generate PR description, Run pre-commit checks
       Location: .github/prompts/prompt-name.prompt.md
```

---

## Detailed Comparison

### Feature Matrix

| Feature | Instructions | Skills | Agents | Prompts |
|---------|-------------|--------|--------|---------|
| **When Active** | Always | On-demand | When selected | On-demand |
| **Applies To** | All files | Specific tasks | Current session | Specific tasks |
| **Glob Patterns** | ✅ Yes | ❌ No | ❌ No | ❌ No |
| **Tool Restrictions** | ❌ No | ❌ No | ✅ Yes | ✅ Yes (optional) |
| **Can Include Scripts** | ❌ No | ✅ Yes | ❌ No | ❌ No |
| **Portability** | VS Code only | Multi-tool ⭐ | VS Code + cloud | VS Code only |
| **Model Selection** | ❌ No | ❌ No | ✅ Yes | ✅ Yes (optional) |
| **Handoffs** | ❌ No | ❌ No | ✅ Yes | ❌ No |
| **File Extension** | `.instructions.md` | `SKILL.md` | `.agent.md` | `.prompt.md` |
| **Invocation** | Automatic | `/skill-name` or auto | Agent dropdown | `/prompt-name` |

### Capability Comparison

| Capability | Instructions | Skills | Agents | Prompts |
|------------|-------------|--------|--------|---------|
| **Enforce Standards** | ⭐⭐⭐ Excellent | ❌ Not intended | ❌ Not intended | ❌ Not intended |
| **Reusable Procedures** | ⚠️ Limited | ⭐⭐⭐ Excellent | ⭐⭐ Good | ⭐ Basic |
| **Include Resources** | ❌ No | ⭐⭐⭐ Yes | ❌ No | ❌ No |
| **Role-Based Behavior** | ❌ No | ❌ No | ⭐⭐⭐ Excellent | ⚠️ Basic |
| **Workflow Orchestration** | ❌ No | ❌ No | ⭐⭐⭐ Excellent | ❌ No |
| **Quick Automation** | ❌ No | ⚠️ Overkill | ⚠️ Overkill | ⭐⭐⭐ Excellent |

---

## Common Scenarios & Recommendations

### Scenario 1: Enforce "Sealed by Default" C# Classes

**Requirement:** All C# classes should be `sealed` unless inheritance is needed.

**Recommended:** Custom Instructions ✅

**Why:**
- This is a **coding standard** that should apply everywhere
- Needs to be **always active** (not on-demand)
- Can use **glob patterns** to target only C# files

**Implementation:**
```markdown
File: .github/instructions/csharp-sealed.instructions.md

---
applyTo: '**/*.cs'
---

# Sealed Classes Standard

Make all classes `sealed` by default. Only mark as non-sealed when inheritance is explicitly required.

Exception: Abstract classes and base classes in inheritance hierarchies.
```

**Alternative Approaches:**
- ❌ Skill: Overkill, Skills are for on-demand capabilities
- ❌ Agent: Wrong tool, agents are for workflows not standards
- ❌ Prompt: Can't enforce automatically, would require manual invocation

---

### Scenario 2: Database Migration Workflow

**Requirement:** Multi-step process for creating, testing, and deploying migrations with validation scripts.

**Recommended:** Agent Skill ✅

**Why:**
- This is a **specialized capability** with multiple steps
- Needs **scripts and templates** (migration SQL, validation scripts)
- Should be **portable** across VS Code, CLI, and cloud
- Task-specific, not role-specific

**Implementation:**
```
.github/skills/database-migration/
├── SKILL.md              # Instructions for migration workflow
├── migration-template.sql # SQL template
├── validation-script.sh   # Validation script
└── examples/
    └── sample-migration.sql
```

**Alternative Approaches:**
- ⚠️ Agent: Could work, but less portable and can't include script resources
- ❌ Instructions: Wrong tool, this is on-demand not always-on
- ❌ Prompt: Can't include scripts/resources, less structured

---

### Scenario 3: Architecture Review Workflow

**Requirement:** Review code for Clean Architecture compliance with read-only access.

**Recommended:** Custom Agent ✅

**Why:**
- This is a **role-based workflow** (Architecture Reviewer persona)
- Needs **read-only tool restrictions** (reviewer shouldn't modify code)
- Requires **structured output** format
- May need **handoffs** to other agents (e.g., handoff to implementer)

**Implementation:**
```markdown
File: .github/agents/architecture-reviewer.agent.md

---
name: architecture-reviewer
description: Reviews code for architectural concerns and boundary violations
tools: ['read', 'search']  # Read-only tools
model: Claude Sonnet 4.5
handoffs:
  - label: Hand off to Implementer
    agent: implementer
    prompt: Address the architectural concerns identified above
---

# Architecture Reviewer

You are an expert software architect...
```

**Alternative Approaches:**
- ⚠️ Skill: Can't restrict tools (reviewer could accidentally modify code)
- ❌ Instructions: Wrong tool, this is on-demand not always-on
- ❌ Prompt: Can't restrict tools or define handoffs

---

### Scenario 4: Generate PR Description

**Requirement:** Before opening a PR, generate description from recent commits.

**Recommended:** Prompt File ✅

**Why:**
- This is a **simple one-off task** done once per PR
- Doesn't need scripts, resources, or tool restrictions
- Quick and lightweight

**Implementation:**
```markdown
File: .github/prompts/pr-description.prompt.md

---
name: pr-description
description: Generate PR description from recent commits
---

# Generate PR Description

Review the recent commits and generate a pull request description including:
1. Summary of changes
2. Key features or fixes
3. Breaking changes (if any)
4. Testing done

Format for markdown.
```

**Alternative Approaches:**
- ⚠️ Skill: Overkill, too complex for simple task
- ⚠️ Agent: Overkill, doesn't need role-based behavior
- ❌ Instructions: Can't be invoked on-demand

---

### Scenario 5: Test Data Generation

**Requirement:** Generate realistic test data for integration tests.

**Recommended:** Agent Skill ✅

**Why:**
- **Specialized capability** that's reusable across many tests
- Can include **templates and examples** (see workshop example)
- Should be **portable** (use in VS Code, CLI, CI/CD)
- Task-specific (not role-specific)

**Workshop Example:**
`.github/skills/test-data-generator/` - See this example in the repository!

**Alternative Approaches:**
- ⚠️ Agent: Could work, but can't include template resources
- ❌ Instructions: Wrong tool, this is on-demand not always-on
- ⚠️ Prompt: Can't include templates, less structured

---

### Scenario 6: Security Audit Review

**Requirement:** Review code for security vulnerabilities before deployment.

**Recommended:** Custom Agent ✅

**Why:**
- **Role-based behavior** (Security Auditor persona)
- Needs **read-only access** (no modifications during audit)
- Requires **specific security checklist** output format
- May have **specific model preference** (e.g., model trained on security)

**Implementation:**
```markdown
---
name: security-auditor
description: Reviews code for security vulnerabilities
tools: ['read', 'search', 'web']  # Can search security databases
model: Claude Sonnet 4.5
---

# Security Auditor

You are a security expert reviewing code for vulnerabilities...

## Security Checklist
- SQL injection risks
- XSS vulnerabilities
- Authentication/authorization flaws
...
```

**Alternative Approaches:**
- ❌ Skill: Can't restrict tools or define security-specific model
- ❌ Instructions: Wrong tool, audits are on-demand not continuous
- ❌ Prompt: Can't restrict tools

---

## Skills vs Agents: The Critical Distinction

This is the most common confusion. Here's the definitive guide:

### When to Use a Skill

✅ **Use a Skill when you need:**
- A **portable capability** that works in multiple environments
- **Scripts, templates, or resources** alongside instructions
- **Task-specific knowledge** (how to do something)
- Something that **any agent** might need to use

❌ **Don't use a Skill when:**
- You need tool restrictions (use Agent)
- It's a role-based persona (use Agent)
- It's a coding standard (use Instructions)
- It's a one-off task (use Prompt)

### When to Use an Agent

✅ **Use an Agent when you need:**
- A **role-based persona** (Architect, Reviewer, Auditor, etc.)
- **Tool restrictions** (read-only, write-only, specific tools)
- **Workflow orchestration** with handoffs between roles
- **Model selection** for specialized tasks

❌ **Don't use an Agent when:**
- You need portable scripts/resources (use Skill)
- It's a coding standard (use Instructions)
- It's a simple task without role context (use Prompt)

### Mental Model

**Skills** = A specialized **toolkit** you give to any agent  
**Agents** = A role-based **specialist** you hire for a workflow

**Example:**
- **Skill:** "Database migration procedures" (toolkit for running migrations)
- **Agent:** "Database Administrator" (DBA persona who uses migration skills)

---

## Combining Customization Types

You can (and should) use multiple customization types together!

### Example: Full Stack Approach

```
Project Customizations:
├── Instructions (Always-on standards)
│   ├── .github/copilot-instructions.md          # Global standards
│   ├── .github/instructions/csharp.instructions.md   # C# conventions
│   └── .github/instructions/test.instructions.md     # Test standards
│
├── Skills (Portable capabilities)
│   ├── .github/skills/test-data-generator/      # Test data skill
│   ├── .github/skills/database-migration/       # Migration skill
│   └── .github/skills/api-integration/          # API testing skill
│
├── Agents (Role-based workflows)
│   ├── .github/agents/architecture-reviewer.agent.md  # Reviews
│   ├── .github/agents/plan.agent.md             # Planning
│   └── .github/agents/implementer.agent.md      # Implementation
│
└── Prompts (Quick tasks)
    ├── .github/prompts/pr-description.prompt.md
    ├── .github/prompts/commit-message.prompt.md
    └── .github/prompts/changelog.prompt.md
```

### How They Work Together

1. **Instructions** set the coding standards for everything
2. **Skills** provide specialized capabilities any agent can use
3. **Agents** orchestrate workflows using skills
4. **Prompts** handle quick one-off tasks

**Example Workflow:**
1. Developer selects `@plan` agent (Custom Agent)
2. Agent uses `test-data-generator` skill (Agent Skill) to propose test fixtures
3. All generated code follows `.github/copilot-instructions.md` (Instructions)
4. After implementation, use `/pr-description` (Prompt) to generate PR

---

## Team Governance Considerations

### Decision Authority

| Customization Type | Who Should Decide? | Impact Level |
|--------------------|-------------------|--------------|
| Instructions | Team Lead + Architect | High (affects all code) |
| Skills | Any developer → Team review | Medium (on-demand, reusable) |
| Agents | Team consensus | Medium (workflow impact) |
| Prompts | Individual developers | Low (personal automation) |

### Review Process

**Instructions:**
- ⚠️ **Require PR review** - Changes affect all generated code
- ✅ **Test thoroughly** - Verify glob patterns work correctly
- ✅ **Document exceptions** - When rules don't apply

**Skills:**
- ✅ **Review for quality** - Verify instructions are clear
- ✅ **Test scripts** - Ensure included resources work
- ⚠️ **Check portability** - Test in multiple environments

**Agents:**
- ✅ **Review for safety** - Verify tool restrictions are appropriate
- ✅ **Test workflows** - Ensure handoffs work correctly
- ⚠️ **Monitor usage** - Track which agents are actually used

**Prompts:**
- ✅ **Light review** - Verify they work as intended
- ✅ **Share useful ones** - Promote good prompts to team

---

## Migration Patterns

### From Ad-hoc Prompts → Customizations

| Current State | Migrate To | When |
|--------------|-----------|------|
| "Always remind me to..." | Instructions | Rule applies universally |
| Copy/paste same prompt | Prompt File | Task is repeated often |
| Multi-turn workflow | Agent | Workflow orchestration needed |
| Shared procedure document | Skill | Includes resources or is portable |

### From One Type → Another

**Instructions → Skill:**
- Rule is complex with multiple steps
- Need to include scripts/templates
- Want portability across environments

**Prompt → Agent:**
- Need tool restrictions
- Workflow involves multiple steps with handoffs
- Need role-based behavior

**Agent → Skill:**
- Don't actually need tool restrictions
- Want portability to CLI/cloud
- It's a capability not a role

---

## Quick Reference Cheat Sheet

| I Need To... | Use... |
|-------------|--------|
| Enforce a coding standard everywhere | Instructions |
| Remind me about conventions in specific files | Instructions with glob |
| Create a reusable multi-step procedure | Skill |
| Include scripts or templates | Skill |
| Make something work in CLI/cloud | Skill |
| Create a role-based reviewer/auditor | Agent |
| Restrict which tools the AI can use | Agent |
| Orchestrate workflow with handoffs | Agent |
| Automate a simple recurring task | Prompt |
| Generate something once (PR, commit msg) | Prompt |

---

## Additional Resources

- [VS Code Copilot Customization Overview](https://code.visualstudio.com/docs/copilot/customization/overview)
- [Agent Skills Documentation](https://code.visualstudio.com/docs/copilot/customization/agent-skills)
- [Custom Agents Documentation](https://code.visualstudio.com/docs/copilot/customization/custom-agents)
- [Custom Instructions Documentation](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)
- [Agent Skills Standard (agentskills.io)](https://agentskills.io/)
- [Workshop Lab 06: Skills & Customization](../labs/lab-06-skills-and-customization.md)

---

## Version History

- **v1.0** (March 30, 2026) - Initial guide created
  - Four customization types
  - Decision tree and comparison tables
  - Common scenarios and recommendations
  - Skills vs Agents differentiation
  - Team governance guidance

---

**Questions or need help deciding?** Review Lab 06 for hands-on exercises and scenarios, or consult with your team's AI/Architecture lead.
