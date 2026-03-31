# Workshop Update Plan - March 2026
## GitHub Copilot Features Modernization

**Date Created:** March 30, 2026  
**Branch:** `feature/copilot-workshop-updates-march-2026`  
**Timeline:** 4 weeks (completion by April 27, 2026)  
**Research Completed:** March 30, 2026

---

## Executive Summary

This document outlines the comprehensive plan to update the "Using AI for Application Development with GitHub Copilot (.NET Edition)" workshop to reflect current GitHub Copilot capabilities as of March 2026. The update addresses significant new features released since December 2024, particularly the **Agent Skills system**, enhanced agent capabilities, and evolved best practices.

**Impact Level:** HIGH - Several fundamental new features require integration  
**Effort Estimate:** 25-35 hours over 4 weeks

---

## Research Findings

### Documentation Sources Reviewed
- VS Code Copilot Customization Docs (Updated: March 25, 2026)
- VS Code Copilot Overview (Updated: March 25, 2026)
- GitHub Copilot Documentation (Current as of March 2026)
- Agent Skills Specification (agentskills.io)

### Critical New Features Identified

#### 1. **Agent Skills System** ⭐ NEW - Major Addition
**Status:** Not covered in current workshop  
**Priority:** CRITICAL

**What It Is:**
- Folders of instructions, scripts, and resources stored in `.github/skills/` 
- SKILL.md format with YAML frontmatter
- Portable across VS Code, GitHub Copilot CLI, and GitHub Copilot coding agent
- Open standard (https://agentskills.io)
- Progressive loading: discovery → instructions → resources
- Can be invoked as slash commands or loaded automatically

**Key Differences from Agents:**
- **Skills:** Task-specific capabilities with scripts/resources, loaded on-demand
- **Agents:** Persistent personas with tool restrictions and model preferences
- **Instructions:** Always-on coding standards and guidelines

**File Structure:**
```markdown
---
name: skill-name
description: Description of what the skill does and when to use it
argument-hint: Optional hint text for slash command
user-invocable: true  # Show as slash command
disable-model-invocation: false  # Allow automatic loading
---

# Skill Instructions

Detailed instructions, guidelines, and examples...
```

**Locations:**
- Project: `.github/skills/`, `.claude/skills/`, `.agents/skills/`
- User: `~/.copilot/skills/`, `~/.claude/skills/`, `~/.agents/skills/`

#### 2. **Enhanced Custom Agents**
**Status:** Partially covered, needs updates  
**Priority:** HIGH

**New Capabilities:**
- **Handoffs:** Guided workflows between agents with pre-filled prompts
- **Agent-scoped Hooks:** Execute commands at lifecycle events (Preview)
- **Subagent Configuration:** Control which agents can be called as subagents
- **Organization-level Agents:** Share agents across GitHub organization
- **Claude Format Support:** `.claude/agents` with `.md` files
- **User-invocable and disable-model-invocation flags:** Fine-grained control

**Updated Frontmatter Properties:**
```yaml
---
name: agent-name
description: What the agent does
tools: ['read', 'search', 'web']
agents: ['*']  # or specific agents, or [] for none
model: "Claude Sonnet 4.5 (copilot)"  # Qualified model name
user-invocable: true  # Show in dropdown
disable-model-invocation: false  # Allow as subagent
handoffs:
  - label: Next Step
    agent: target-agent
    prompt: Context for next agent
    send: false
    model: "GPT-5 (copilot)"
hooks:  # Preview feature
  - event: fileCreated
    command: echo "File created"
---
```

**Model Naming Convention:**
- Format: "Model Name (vendor)"
- Examples: "Claude Sonnet 4.5 (copilot)", "GPT-5 (copilot)", "GPT-4o (copilot)"
- Note: "Claude Sonnet 4" in current workshop files should be verified/updated

#### 3. **New Slash Commands**
**Status:** Partially documented, several missing  
**Priority:** MEDIUM

**New Commands to Add:**
- `/init` - Generate .github/copilot-instructions.md for project
- `/create-prompt` - Generate prompt file with AI
- `/create-instruction` - Generate instruction file with AI
- `/create-skill` - Generate agent skill with AI
- `/create-agent` - Generate custom agent with AI
- `/create-hook` - Generate hook configuration with AI
- `/skills` - Quick access to Configure Skills menu
- `/agents` - Quick access to Configure Custom Agents menu

**Currently Documented:** `/explain`, `/fix`, `/tests`, `/doc`, `/refactor`, `/new`, `/clear`

#### 4. **Prompt Files** ⭐ NEW Feature
**Status:** Not covered  
**Priority:** MEDIUM

**What It Is:**
- `.prompt.md` files for repeatable one-off tasks
- Simpler than skills (no scripts/resources)
- Stored in `.github/prompts/` or `~/.copilot/prompts/`
- Available as slash commands

**When to Use:**
- Prompt Files: One-off tasks without tool restrictions
- Skills: Portable capabilities with scripts and resources
- Agents: Persistent personas with tool restrictions

#### 5. **Additional New Features**
**Status:** Not covered  
**Priority:** LOW to MEDIUM (awareness/overview level)

- **MCP Servers:** Model Context Protocol for external tool integration
- **Hooks:** Execute shell commands at lifecycle events
- **Agent Plugins:** Pre-packaged bundles from marketplace
- **Chat Customizations Editor:** Centralized UI for managing all customizations
- **Parent Repository Discovery:** Monorepo support (chat.useCustomizationsInParentRepositories)
- **Sessions Management:** Multiple parallel agent sessions
- **Plan Agent:** Built-in planning agent
- **Background Agents:** For autonomous tasks
- **Cloud Agents:** For PR-based collaboration
- **Third-party Agents:** Anthropic, OpenAI providers

#### 6. **Interaction Model Evolution**
**Status:** Covered but simplified  
**Priority:** MEDIUM

**Current Reality (More Nuanced):**
- **Agent Mode** can run in three contexts:
  - **Local** - Interactive in VS Code
  - **Background** - Autonomous tasks
  - **Cloud** - Team collaboration via PRs
- **Third-party agents** supported (Anthropic, OpenAI)
- **Plan → Execute workflow** as standard pattern

---

## Gap Analysis

### Current Workshop State

| Feature/Topic | Current Coverage | Gap Level | Action Required |
|---------------|------------------|-----------|-----------------|
| **Agent Skills** | ❌ Not mentioned | CRITICAL | Add Lab 5.5 + integrate throughout |
| **Prompt Files** | ❌ Not mentioned | HIGH | Add to Lab 5.5 or overview |
| **Handoffs** | ❌ Not mentioned | MEDIUM | Add to Lab 8 (Agent Design) |
| **New Slash Commands** | ⚠️ Partial (7/15) | MEDIUM | Update facilitator guide + labs |
| **Model Naming** | ⚠️ Uses "Claude Sonnet 4" | LOW | Verify and update all .agent.md files |
| **Agent Hooks** | ❌ Not mentioned | LOW | Brief mention in advanced topics |
| **MCP Servers** | ❌ Not mentioned | LOW | Awareness mention in overview |
| **Agent Plugins** | ❌ Not mentioned | LOW | Awareness mention in overview |
| **Customization Editor** | ❌ Not mentioned | LOW | Add to facilitator setup tips |
| **Organization Agents** | ❌ Not mentioned | LOW | Add to agent governance guide |
| **Claude Format** | ❌ Not mentioned | LOW | Brief mention in agent design |
| **User-invocable Flags** | ❌ Not mentioned | LOW | Add to Lab 8 frontmatter section |
| **Sessions Management** | ❌ Not mentioned | LOW | Optional mention in Part 2 intro |

### Workshop Strengths (No Changes Needed)

✅ TDD fundamentals and workflow  
✅ Clean Architecture and DDD patterns  
✅ Ask/Edit/Agent interaction model concepts  
✅ Custom agents basics (.agent.md format)  
✅ Copilot Instructions pattern  
✅ `.instructions.md` with `applyTo` patterns  
✅ Lab structure and hands-on approach  
✅ .NET 9 and modern tooling  

---

## Detailed Update Plan

### Phase 1: Foundation Updates (Week 1)

#### Task 1.1: Update All Model References
**Files:** All `.agent.md` files in `.github/agents/`  
**Action:** Verify and standardize model references

**Current State:** `model: Claude Sonnet 4`  
**Expected Format:** `model: "Claude Sonnet 4.5 (copilot)"` or verify current naming

**Files to Update:**
- `.github/agents/architecture-reviewer.agent.md`
- `.github/agents/backlog-generator.agent.md`
- `.github/agents/test-strategist.agent.md`
- `.github/agents/api-architect.agent.md`
- `.github/agents/architect.agent.md`
- `.github/agents/Check.agent.md`
- `.github/agents/ellie.agent.md`
- `.github/agents/expert-dotnet-software-engineer.agent.md`
- `.github/agents/plan.agent.md`

#### Task 1.2: Update Slash Commands Documentation
**Files:**
- `docs/FACILITATOR_GUIDE.md` (Section 0.5)
- `docs/FACILITATOR_GUIDE_PART2.md`
- `docs/labs/lab-04-testing-documentation-workflow.md`

**Add:**
- `/init` - Initialize project with custom instructions
- `/create-*` commands for generating customization files
- `/skills` and `/agents` quick access commands

---

### Phase 2: Major Content Addition - Skills (Week 2)

#### Task 2.1: Create New Lab 5.5 - Skills & Customization Hierarchy
**New File:** `docs/labs/lab-05.5-skills-and-customization.md`  
**Duration:** 25-30 minutes  
**Insert:** Between Lab 5 (Interaction Models) and Lab 6 (Custom Agents)

**Lab Structure:**

**Part 1: Understanding the Customization Landscape (10 min)**
- What are Skills vs Agents vs Prompt Files vs Instructions?
- Decision tree: When to use each
- Comparison table
- Portability considerations

**Part 2: Exploring Agent Skills (10 min)**
- Review a pre-built skill
- Understand SKILL.md format
- See progressive loading in action
- Use skill as slash command vs automatic invocation

**Part 3: Hands-On Exercise (5-10 min)**
- Scenario-based questions: Which customization type?
- Brief walkthrough of skill creation (conceptual)
- Optional: Create simple skill with `/create-skill`

**Success Criteria:**
- Participants can differentiate between all customization types
- Participants understand when to use skills vs agents
- Participants know how to invoke skills
- Foundation for Labs 6-9 enhanced

#### Task 2.2: Create Example Skill for Workshop
**New Directory:** `.github/skills/test-data-generator/`  
**New File:** `.github/skills/test-data-generator/SKILL.md`  
**Purpose:** Demonstrate skill structure for Lab 5.5

**Content:**
- YAML frontmatter with all standard properties
- Clear instructions for generating test data
- Optional: Include example scripts or templates
- Show how skills reference local resources

#### Task 2.3: Create Skills vs Agents vs Instructions Comparison Doc
**New File:** `docs/guides/customization-decision-guide.md`  
**Purpose:** Reference material for choosing customization approach

**Sections:**
- Overview of all customization types
- Decision tree flowchart
- Side-by-side comparison table
- Real-world examples
- Team governance considerations

---

### Phase 3: Enhanced Agent Content (Week 2-3)

#### Task 3.1: Update Lab 8 - Agent Design
**File:** `docs/labs/lab-08-agent-design.md`  
**Updates:**

1. **Add Handoffs Section:**
   - What are handoffs and why use them
   - How to define handoffs in frontmatter
   - Example: Plan → Implement → Review workflow
   - Exercise: Design a handoff chain

2. **Expand Frontmatter Documentation:**
   - Add `user-invocable` and `disable-model-invocation` flags
   - Explain `agents` property for subagent control
   - Document `handoffs` syntax
   - Mention `hooks` (preview) briefly

3. **Add "Agent vs Skill" Decision Point:**
   - When to create an agent vs a skill
   - Refactoring considerations
   - Tool restrictions use case

#### Task 3.2: Update Lab 9 - Capstone
**File:** `docs/labs/lab-09-capstone-build-agent.md`  
**Updates:**

1. **Add Skill Option:**
   - Option to build either a custom agent OR a skill
   - Skill-specific success criteria
   - Skill template option

2. **Add Handoff Consideration:**
   - For agents: Consider adding handoffs to other agents
   - Example handoff scenarios

#### Task 3.3: Update Agent Governance Guide
**File:** `docs/guides/agent-governance.md`  
**Updates:**

1. **Add Skills Section:**
   - Governance for skills alongside agents
   - Skills repository structure
   - Review process for skills

2. **Add Organization-Level Agents:**
   - How to share agents at GitHub organization level
   - Setting: `github.copilot.chat.organizationCustomAgents.enabled`

3. **Add Handoff Governance:**
   - Guidelines for designing handoff chains
   - Preventing circular handoffs

---

### Phase 4: Interaction Models Update (Week 3)

#### Task 4.1: Update Lab 5 - Interaction Models
**File:** `docs/labs/lab-05-interaction-models.md`  
**Updates:**

1. **Expand Agent Mode Section:**
   - Agent mode runs in three contexts: Local, Background, Cloud
   - When to use each context
   - Third-party agent support (brief mention)

2. **Add Plan → Execute Pattern:**
   - Built-in Plan agent
   - Plan before implementation workflow
   - Integration with Agent mode

3. **Forward Reference to Skills:**
   - "In the next lab, we'll explore Skills which work with Agent mode..."
   - Link to Lab 5.5

---

### Phase 5: Facilitator Guide Updates (Week 3-4)

#### Task 5.1: Update Part 1 Facilitator Guide
**File:** `docs/FACILITATOR_GUIDE.md`  
**Updates:**

1. **Section 0.5 - Copilot Features Overview:**
   - Add new slash commands
   - Mention Chat Customizations editor
   - Update slash command examples

2. **Troubleshooting Section:**
   - Add: "Why isn't my agent/skill being loaded?"
   - Chat customization diagnostics view
   - Parent repository discovery setting

#### Task 5.2: Update Part 2 Facilitator Guide
**File:** `docs/FACILITATOR_GUIDE_PART2.md`  
**Updates:**

1. **Module 0 - Kickoff:**
   - Add Skills overview (2 min)
   - Show customization hierarchy diagram

2. **New Module 1.5 - Skills (after Interaction Models):**
   - Full facilitation guide for Lab 5.5
   - Demo script for Skills
   - Common questions and answers

3. **Module 2 - Custom Agents:**
   - Reference Skills comparison from Lab 5.5
   - Shorter intro since foundation laid earlier

4. **Schedule Update:**
   - Add 25 min for Lab 5.5
   - Adjust subsequent timings
   - Total should stay ~3 hours (may need to tighten other sections)

#### Task 5.3: Update Main README
**File:** `README.md`  
**Updates:**

1. **Learning Objectives - Part 2:**
   - Add: "Understand Skills, Agents, Prompt Files, and Instructions hierarchy"
   - Add: "Create agent skills for portable, specialized capabilities"
   - Add: "Design agent handoffs for multi-step workflows"

2. **Labs Overview:**
   - Add Lab 5.5 to table
   - Update descriptions to mention Skills

---

### Phase 6: Supporting Materials (Week 4)

#### Task 6.1: Update Architecture Diagrams
**File:** `docs/design/diagrams/agent-architecture.md`  
**Action:** Add Skills layer to customization architecture

**Create New Diagram:** `docs/design/diagrams/customization-hierarchy.md`
- Visual representation of Instructions → Skills → Agents → Prompt Files
- Decision flowchart
- Mermaid diagram format

#### Task 6.2: Create Skills Catalog
**New File:** `docs/guides/skills-catalog.md`  
**Purpose:** Registry of example skills (similar to custom-agent-catalog.md)

**Content:**
- List of skills in `.github/skills/`
- Purpose and usage for each
- When to use each skill
- Community skills references

#### Task 6.3: Update Presentations
**Files:** 
- `docs/presentations/advanced-github-copilot.md`
- `docs/presentations/lunch-and-learn-executive.md` (if relevant)

**Updates:**
- Add slides for Skills
- Update customization landscape
- Add handoffs concept
- Update any outdated screenshots or references

---

### Phase 7: Testing & Validation (Week 4)

#### Task 7.1: Workshop Dry Run
**Action:** Execute all labs in sequence

**Checklist:**
- [ ] All code examples compile and run
- [ ] All agents invoke correctly
- [ ] Example skill loads and works
- [ ] New slash commands documented correctly
- [ ] All internal links work
- [ ] Lab timings are accurate

#### Task 7.2: Agent Validation
**Action:** Test all `.agent.md` files

**Checklist:**
- [ ] Model references are current and correct
- [ ] All agents appear in dropdown
- [ ] Agents can be invoked
- [ ] Handoffs work (if added)
- [ ] Tool restrictions function correctly

#### Task 7.3: Documentation Review
**Action:** Full documentation pass

**Checklist:**
- [ ] No broken links
- [ ] Consistent terminology
- [ ] All new content integrated smoothly
- [ ] Facilitator guides updated
- [ ] README reflects all changes

---

### Phase 8: Change Tracking & Cleanup Evaluation (Week 4)

#### Task 8.1: Generate Change Report
**Action:** Create comprehensive list of all modified files during update

**Deliverable:** `docs/workshop-update-changes-march-2026.md`

**Content:**
- List all files created (with purpose)
- List all files modified (with summary of changes)
- Git diff statistics
- Change categories (new features, updates, fixes)

#### Task 8.2: Identify Untouched Files
**Action:** Generate inventory of files NOT modified during update

**Method:**
```bash
# Generate list of all markdown/agent/instruction files
# Compare against changed files
# Produce untouched files inventory
```

**Deliverable:** Section in change report listing untouched files by category:
- Documentation files (docs/)
- Agent definitions (.github/agents/)
- Instruction files (.github/instructions/)
- Presentations (docs/presentations/)
- Requirements/notes (docs/notes/, docs/requirements/)
- Other

#### Task 8.3: Evaluate Untouched Files for Relevance
**Action:** Review each untouched file and categorize

**Categories:**
1. **Keep - Still Relevant:** File serves current purpose
2. **Keep - Reference Material:** Historical or supplementary
3. **Update Needed:** File is outdated but should be updated
4. **Archive Candidate:** Outdated, low value, consider moving
5. **Remove Candidate:** No longer relevant, consider deletion

**Deliverable:** Recommendations table in change report

**Example Format:**
```markdown
| File | Category | Reason | Recommendation |
|------|----------|--------|----------------|
| docs/notes/20251119-demo-notes.md | Archive Candidate | Demo notes from Nov 2024 | Move to archive/ folder |
| coverage/ | Remove Candidate | Generated files | Add to .gitignore, remove from repo |
| docs/api/tasks.md | Keep - Still Relevant | API documentation | No action needed |
```

#### Task 8.4: Create Cleanup Action Plan (Optional)
**Action:** If significant cleanup identified, create separate cleanup plan

**Conditions for separate plan:**
- 10+ files recommended for removal
- Structural reorganization beneficial
- Breaking changes to file paths

**Deliverable:** `docs/workshop-cleanup-plan-march-2026.md` (if needed)

**Includes:**
- Files/folders to remove with justification
- Files/folders to archive
- Updates to .gitignore
- Impact assessment
- Migration/redirect strategy if needed

---

## File Inventory

### Files to Create (New)
1. `docs/labs/lab-05.5-skills-and-customization.md` ⭐
2. `docs/guides/customization-decision-guide.md` ⭐
3. `docs/guides/skills-catalog.md`
4. `docs/design/diagrams/customization-hierarchy.md`
5. `.github/skills/test-data-generator/SKILL.md` (example)
6. `.github/skills/test-data-generator/README.md`
7. **`docs/workshop-update-changes-march-2026.md`** ⭐ (change tracking report)
8. **`docs/workshop-cleanup-plan-march-2026.md`** (if needed)

### Files to Update (Major Changes)
1. `docs/labs/lab-08-agent-design.md` - Add handoffs, flags, agent vs skill
2. `docs/labs/lab-09-capstone-build-agent.md` - Add skill option
3. `README.md` - Add Lab 5.5, update objectives
4. `docs/FACILITATOR_GUIDE.md` - Slash commands, troubleshooting
5. `docs/FACILITATOR_GUIDE_PART2.md` - Add Module 1.5, update schedule
6. `docs/labs/README.md` - Add Lab 5.5 to table

### Files to Update (Minor Changes)
1. All `.github/agents/*.agent.md` files - Verify model names
2. `docs/labs/lab-05-interaction-models.md` - Expand Agent mode
3. `docs/labs/lab-06-custom-agents-intro.md` - Reference Skills
4. `docs/labs/lab-04-testing-documentation-workflow.md` - Update slash commands
5. `docs/guides/agent-governance.md` - Add Skills, org agents
6. `docs/guides/agent-design-guide.md` - Add Skills section
7. `docs/design/diagrams/agent-architecture.md` - Add Skills
8. `docs/presentations/advanced-github-copilot.md` - Add Skills slides

### Files Requiring Verification Only
1. All lab files - Check for outdated references
2. `docs/guides/custom-agent-catalog.md` - Ensure descriptions current
3. `.github/copilot-instructions.md` - Still current
4. `.github/instructions/*.instructions.md` - Still current

---

## Risk Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Skills system works differently than documented | Low | High | Validate example skill in VS Code before finalizing Lab 5.5 |
| Model naming causes agent failures | Medium | Medium | Test all agents after model name updates |
| Lab 5.5 increases Part 2 duration beyond 3 hours | Medium | Medium | Tighten other labs or make Skills section 15-20 min |
| Handoff feature not available/working | Low | Low | Mark as optional or "if available" feature |
| Breaking changes in newer Copilot versions | Low | Medium | Document tested version, regular reviews |

---

## Success Criteria

### Must-Have (Critical)
- [ ] Lab 5.5 created and integrated
- [ ] Skills vs Agents vs Instructions clearly differentiated
- [ ] All model references verified and updated
- [ ] New slash commands documented
- [ ] Example skill working and tested
- [ ] All labs tested end-to-end
- [ ] **Change report generated with all modifications documented**
- [ ] **Untouched files identified and evaluated**

### Should-Have (High Value)
- [ ] Handoffs documented and demonstrated
- [ ] Customization decision guide created
- [ ] Agent governance updated for Skills
- [ ] Facilitator guides fully updated
- [ ] All diagrams updated with Skills
- [ ] **Cleanup recommendations provided for untouched files**

### Nice-to-Have (Enhancement)
- [ ] Skills catalog created
- [ ] Multiple example skills
- [ ] Advanced handoff patterns
- [ ] MCP/Hooks awareness sections
- [ ] **Cleanup action plan created (if significant cleanup needed)**

---

## Timeline

### Week 1 (Apr 1-7): Foundation
- Research complete ✅
- Update plan document ✅
- Update all model references
- Document slash commands
- Verify agent format changes

### Week 2 (Apr 8-14): Skills Content
- Create Lab 5.5
- Create example skill
- Create customization decision guide
- Begin agent enhancements

### Week 3 (Apr 15-21): Agent & Labs Updates
- Update Labs 8 and 9
- Update Lab 5
- Update facilitator guides
- Update governance docs

### Week 4 (Apr 22-27): Polish, Testing & Evaluation
- Create supporting diagrams
- Update presentations
- Full workshop dry run
- Documentation review
- **Generate change report**
- **Identify and evaluate untouched files**
- **Create cleanup recommendations**

---

## Next Steps

1. ✅ Complete research (DONE)
2. ✅ Create update plan (DONE - this document)
3. **Review plan with stakeholder**
4. Begin Phase 1: Foundation Updates
5. Proceed through phases sequentially
6. Test incrementally after each phase
7. **Generate change report and evaluate untouched files (Phase 8)**
8. **Review cleanup recommendations before finalizing**

---

## Stakeholder Decisions

**Date Finalized:** March 30, 2026

### Decisions Made:

1. **Lab 5.5 Duration:** ✅ **25-30 minutes** (full hands-on version)
   - Part 1: Understand customization types (10 min)
   - Part 2: Explore a pre-built skill (10 min)
   - Part 3: Hands-on scenario exercise (5-10 min)

2. **Skills Depth:** ✅ **Conceptual + hands-on exploration**
   - Explain Skills and customization types
   - Explore a pre-built skill in detail
   - Use a skill via slash command
   - See automatic skill invocation
   - Scenario-based decision exercises
   - NOT including: Creating skills from scratch (saved for optional capstone work)

3. **Handoffs Priority:** ✅ **Core content (Lab 8)**
   - Required conceptual understanding (10 min)
   - Simple hands-on: Add one handoff to an existing agent (5 min)
   - Demonstrates Plan → Implement workflow pattern
   - Part of agent design best practices

4. **Capstone Options:** ✅ **Agent with optional Skill add-on**
   - Primary focus: Everyone builds an agent
   - Extension work: Advanced participants can create a supporting skill
   - Single primary instruction path for easier facilitation
   - Skills creation remains available but not required

5. **Model Names:** ✅ **`model: Claude Sonnet 4.5`**
   - Format: Simple model name without vendor suffix
   - Apply to all `.agent.md` files in Phase 1
   - Example: `model: Claude Sonnet 4.5`

---

## Future Enhancements (Post-April 2026)

### Java Workshop Variants
**Status:** Planned - Not included in current scope  
**Effort:** TBD (separate planning required)

**Scope:**
- Create Java versions of all labs (Labs 1-9)
- Adapt Clean Architecture examples to Java/Spring Boot
- Update agent instructions for Java best practices
- Create Java-specific copilot-instructions.md
- Maintain parallel workshop tracks (.NET and Java)

**Considerations:**
- Spring Boot vs Quarkus framework choice
- JUnit vs TestNG testing framework
- Maven vs Gradle build tool
- Java-specific DDD patterns and conventions
- Code organization (packages vs namespaces)

**Recommendation:** Complete .NET updates first, then assess Java workshop as separate project with dedicated planning phase.

---

## Appendix: Key Resources

### Official Documentation
- VS Code Copilot Customization: https://code.visualstudio.com/docs/copilot/customization/overview
- Agent Skills: https://code.visualstudio.com/docs/copilot/customization/agent-skills
- Custom Agents: https://code.visualstudio.com/docs/copilot/customization/custom-agents
- Agent Skills Standard: https://agentskills.io

### Reference Repositories
- Awesome Copilot: https://github.com/github/awesome-copilot
- Anthropic Skills: https://github.com/anthropics/skills

### Workshop Branch
- `feature/copilot-workshop-updates-march-2026`
