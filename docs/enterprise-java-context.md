# Enterprise Java Workshop Context

**AI Coding Workshop - Enterprise Java Edition**

> Accelerate enterprise Java modernization with GitHub Copilot and custom agents

---

## Workshop Overview

### Purpose
Enable enterprise Java development teams to leverage AI-assisted development for legacy system modernization, specifically transitioning from Mule ESB to Spring Boot microservices.

### Target Audience
- Technical leaders in enterprise Java organizations
- Development teams working on legacy system modernization
- Architects planning Mule ESB → Spring Boot migrations
- Engineering managers seeking productivity improvements

### Duration
**6 hours total** (2× 3-hour sessions)
- **Part 1**: Fundamentals (3 hours)
- **Part 2**: Advanced Practices (3 hours)

### Format
Hands-on labs with **dual-stack examples** (both .NET and Spring Boot), allowing Java and .NET teams to learn together while focusing on their preferred technology stack.

---

## Why This Workshop Matters

### Enterprise Java Modernization Challenges

**Legacy System Complexity:**
- Mule ESB applications with years of accumulated business logic
- Stored procedures embedded in database layers
- Monolithic architectures difficult to scale
- Limited observability and monitoring
- Technical debt accumulation

**Migration Risks:**
- Business logic extraction from legacy code
- Integration point identification
- Data model transformation
- Maintaining system availability during

 transition
- Team skill gaps (Mule ESB → Spring Boot)

### Demonstrated Benefits

**Productivity Improvements:**
- **3-5x faster development** with AI-assisted coding
- **70-80% reduction** in boilerplate code writing
- **Faster debugging** with AI-powered analysis
- **Accelerated learning** of Spring Boot patterns

**Quality Improvements:**
- **70%+ test coverage** achievable with AI-generated tests
- **Consistent architecture** via custom agents
- **Reduced technical debt** through automated reviews
- **Better documentation** generated alongside code

**Business Impact:**
- Faster time-to-market for new features
- Reduced modernization project timelines
- Lower risk through comprehensive testing
- Improved system observability

---

## AI Maturity Model

Understanding where your team is today and where this workshop will take you:

### Level 1: Ad-hoc AI Usage ⚠️
**Current State:**
- Developers copy-paste from ChatGPT
- Inconsistent code quality
- No team standards
- Results vary by individual skill

**Characteristics:**
- Manual context switching
- One-off prompts
- No reusable patterns
- Limited productivity gains

### Level 2: Systematic AI Usage ✅
**With GitHub Copilot:**
- AI integrated into IDE
- Code suggestions in real-time
- Consistent developer experience
- 15-25% productivity improvement

**Characteristics:**
- Copilot enabled for all developers
- Basic prompt engineering skills
- Some team conventions emerging
- First productivity gains visible

### Level 3: AI-First Workflows 🎯
**Workshop Target State:**
- Custom agents for specialized tasks
- Copilot Instructions (steering documents)
- Team-shared patterns and practices
- 40-60% productivity improvement

**Characteristics:**
- **Custom Agents**: Architecture Reviewer, Quality Gate, Test Coverage, Modernization
- **Steering Documents**: Spring Boot instructions enforce standards
- **Pattern Libraries**: Reusable migration patterns
- **Workflow Integration**: AI at every development stage

### Level 4: AI Orchestration 🚀
**Future State:**
- Multi-agent systems
- Automated workflows end-to-end
- Self-improving agents
- 70%+ productivity improvement

**Characteristics:**
- Agents collaborate autonomously
- Continuous learning from feedback
- Organization-wide AI platform
- Measurable ROI on AI investment

**This workshop moves teams from Level 2 → Level 3**

---

## Success Stories

### Developer A: Backend Engineer
**Background:** 10 years Java experience, new to Spring Boot  
**Challenge:** Migrating Mule ESB payment processing flow

**Results:**
- ✅ **3-5x productivity improvement** on migration tasks
- ✅ Extracted business logic from 8 Mule flows in 2 weeks
- ✅ Generated comprehensive test suites (75% coverage)
- ✅ Learned Spring Boot patterns through AI assistance

**Quote:** "Copilot became my pair programmer. The modernization agent extracted requirements I didn't even know existed in the Mule flows."

### Development Team: Enterprise Integration
**Background:** Team of 6, modernizing order management system  
**Challenge:** 50+ Mule ESB flows, complex integrations

**Results:**
- ✅ **70%+ test coverage** achieved across all microservices
- ✅ **2x faster** story completion after adopting custom agents
- ✅ Reduced code review cycles from days to hours
- ✅ Consistent architecture across all services

**Quote:** "The Quality Gate and Architecture Reviewer agents caught issues before human review. Our tech debt actually decreased during the migration."

### QE Team: Testing Automation
**Background:** Manual testing team transitioning to automation  
**Challenge:** Writing JUnit 5 and Mockito tests from scratch

**Results:**
- ✅ **80% faster** test authoring with Test Coverage agent
- ✅ Learned testing best practices through AI feedback
- ✅ Built reusable test fixtures and utilities
- ✅ Shifted left on quality with automated coverage checks

### Technical Leader: Architecture Governance
**Background:** Lead architect for modernization initiative  
**Challenge:** Ensuring consistent patterns across 12 teams

**Results:**
- ✅ Created organization-wide Copilot Instructions
- ✅ Custom agents encode architectural decisions
- ✅ Reduced architecture review burden by 60%
- ✅ Trained 50+ developers in 3 months through Communities of Practice

---

## Workshop Structure

### Part 1: Fundamentals (3 hours)

**Module 1-1: GitHub Copilot Features (45 min)**
- Code completion and suggestions
- Chat-driven development
- Inline chat for quick edits
- Context awareness and file references

**Module 1-2: Copilot Instructions (45 min)**
- Steering documents for consistency
- Spring Boot instructions (`.github/instructions/springboot.instructions.md`)
- Context-aware instruction loading
- Team standards enforcement

**Module 1-3: TDD with AI (45 min)**  
*Lab 1: Test-Driven Development with Copilot*
- Writing tests first with AI assistance
- Red-Green-Refactor cycle
- JUnit 5 + Mockito patterns
- Spring Boot testing strategies

**Module 1-4: Requirements → Code (45 min)**  
*Lab 2: From Requirements to Spring Boot Code*
- Translating user stories to code
- Domain-Driven Design with AI
- Clean Architecture patterns
- Repository pattern (no stored procedures!)

### Part 2: Advanced Practices (3 hours)

**Module 2-1: Code Generation & Refactoring (45 min)**  
*Lab 3: Generation and Refactoring with Spring Boot*
- REST API generation
- Refactoring legacy patterns
- DataWeave → Java business logic
- Object Calisthenics with AI

**Module 2-2: Custom Agents Introduction (30 min)**  
*Lab 6: Skills and Customization*
- Architecture Reviewer agent
- Quality Gate agent
- Test Coverage agent
- Modernization agent (Mule → Spring Boot)

**Module 2-3: Workflow Automation (30 min)**
*Lab 8: Workflow Agents*
- Multi-step agent workflows
- Human-in-the-loop patterns
- Requirements extraction from Mule ESB

**Module 2-4: Agent Design (45 min)**  
*Lab 9: Agent Design Principles*
- Design patterns for agents
- Role-based scoping
- Testing and iteration

**Module 2-5: Capstone (30 min)**  
*Lab 10: Build Production-Ready Agent*
- Design your own agent
- Modernization use cases
- Team-specific customization

---

## Pre-Workshop Setup

### Required Tools

**Core Development Environment:**
- **VS Code** (latest stable version)
- **Dev Containers extension** for VS Code
- **Docker Desktop** (running and configured)
- **Git** (2.x or later)
- **GitHub account** with Copilot subscription

**Java Development:**
- **Java 21** (LTS) - verify: `java -version`
- **Maven 3.9+** - verify: `mvn -version`
- **Gradle 8+** (optional but recommended)

**Database Tools:**
- **PostgreSQL client** tools (psql, pg_admin)
- **H2 Database** (included in Spring Boot project)

### Repository Setup

```bash
# Clone the workshop repository
git clone https://github.com/centricconsulting/ai-coding-workshop.git
cd ai-coding-workshop

# Open in VS Code
code .

# Select Dev Container
# Command Palette (Cmd/Ctrl+Shift+P)
# > Dev Containers: Reopen in Container
# > Select: springboot-participant
```

**Container Options:**
- `springboot-participant` - Spring Boot only (recommended for Java-focused teams)
- `dotnet-participant` - .NET only
- `maintainer` - Both stacks (for facilitators/mixed teams)

### Verify Setup

```bash
# Inside the dev container:

# Java version (should show 21.x)
java -version

# Maven version (should show 3.9.x)
mvn -version

# Build Spring Boot project
cd src-springboot
mvn clean install

# Expected: BUILD SUCCESS, 102 tests pass

# Run application
mvn spring-boot:run -pl taskmanager-api

# Verify http://localhost:8080/actuator/health returns {"status":"UP"}
```

### Troubleshooting

**Issue: Container won't build**
```bash
# Rebuild without cache
# Command Palette >  Dev Containers: Rebuild Container Without Cache
```

**Issue: Java version incorrect**
```bash
# Check JAVA_HOME
echo $JAVA_HOME
# Should show: /usr/local/sdkman/candidates/java/current

# SDKMan installed? 
sdk list java
```

**Issue: Maven build fails**
```bash
# Clean Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install -U
```

**Issue: Port 8080 already in use**
```bash
# Find and kill process using port 8080
lsof -ti:8080 | xargs kill -9

# Or use different port in application-dev.yml
```

---

## Learning Path

### Week 1: Workshop Completion
**Goal:** Hands-on proficiency with AI-assisted development

**Activities:**
- Complete all 10 labs
- Build 2-3 custom agents  
- Practice TDD with AI assistance
- Generate Spring Boot code from requirements

**Deliverables:**
- Working Spring Boot application
- Custom agents for your team
- Personal Copilot Instructions

### Week 2: Real-World Application
**Goal:** Apply skills to actual modernization work

**Activities:**
- Use Modernization agent on real Mule ESB flows
- Extract requirements from legacy systems
- Implement first microservice with AI assistance
- Validate with Architecture Reviewer and Quality Gate agents

**Deliverables:**
- Requirements documentation from legacy code
- First migrated microservice
- Initial pattern library entries

### Week 3+: Scaling & Teaching
**Goal:** Become AI pioneer and train others

**Activities:**
- Establish Community of Practice
- Share custom agents with broader team
- Train other developers using workshop materials
- Contribute patterns to team library
- Measure productivity improvements

**Deliverables:**
- Trained team members (5-10 developers)
- Organization-wide Copilot Instructions
- Expanded pattern library
- ROI metrics and case studies

---

## Post-Workshop Deliverables

Participants will leave the workshop with:

✅ **Hands-on experience** with all GitHub Copilot features  
✅ **Custom agents** ready for production use:
   - Architecture Reviewer (validates design)
   - Quality Gate (enforces standards)
   - Test Coverage (identifies gaps)
   - Modernization (extracts Mule ESB requirements)

✅ **Spring Boot Sterling Document** template for team customization  
✅ **Pattern library foundation** for Mule → Spring Boot migrations  
✅ **Training materials** to teach other team members  
✅ **Agent design skills** to create organization-specific agents  
✅ **Productivity measurement baseline** for ROI tracking

---

## Communities of Practice Strategy

### Phase 1: Pioneer (Workshop Participants)
**Role:** Technical leaders who complete the workshop  
**Activities:**
- Master AI-assisted development
- Build custom agents
- Document patterns
- Measure productivity gains

### Phase 2: Early Adopters (Weeks 2-4)
**Role:** Developers trained by pioneers  
**Activities:**
- Use agents built by pioneers
- Provide feedback on patterns
- Contribute to pattern library
- Validate productivity improvements

### Phase 3: Community Growth (Months 2-3)
**Role:** Broader development organization  
**Activities:**
- Adopt team-wide Copilot Instructions
- Use shared agents and patterns
- Contribute improvements
- Share success stories

### Phase 4: Organizational Scale (Months 3+)
**Role:** Enterprise-wide adoption  
**Activities:**
- Multi-team collaboration on agents
- Central pattern library
- Measurable ROI across organization
- Continuous improvement culture

---

## Resources

### Workshop Materials
- **Repository:** https://github.com/centricconsulting/ai-coding-workshop
- **Facilitator Guides:** [Part 1](./FACILITATOR_GUIDE.md) | [Part 2](./FACILITATOR_GUIDE_PART2.md)
- **Lab Index:** [docs/labs/README.md](./labs/README.md)

### Spring Boot Resources
- **Copilot Instructions:** [.github/instructions/springboot.instructions.md](../.github/instructions/springboot.instructions.md)
- **Pattern Translation Guide:** [docs/guides/dotnet-to-springboot-patterns.md](./guides/dotnet-to-springboot-patterns.md)
- **TaskManager Reference Implementation:** [src-springboot/](../src-springboot/)
- **DevContainer Test Plan:** [docs/DEVCONTAINER_TEST_PLAN.md](./DEVCONTAINER_TEST_PLAN.md)

### Custom Agents
- **Architecture Reviewer:** [.github/agents/architecture-reviewer.agent.md](../.github/agents/architecture-reviewer.agent.md)
- **Quality Gate:** [.github/agents/quality-gate.agent.md](../.github/agents/quality-gate.agent.md)
- **Test Coverage:** [.github/agents/test-coverage.agent.md](../.github/agents/test-coverage.agent.md)
- **Modernization:** [.github/agents/modernization.agent.md](../.github/agents/modernization.agent.md)
- **Agent Catalog:** [docs/guides/custom-agent-catalog.md](./guides/custom-agent-catalog.md)

### Pattern Library
- **Modernization Patterns:** [docs/patterns/modernization/](./patterns/modernization/) *(Issue #29)*

### Additional Documentation
- **Testing Strategy:** [docs/design/testing-strategy.md](./design/testing-strategy.md)
- **Architecture Overview:** [docs/design/architecture.md](./design/architecture.md)
- **Agent Design Guide:** [docs/guides/agent-design-guide.md](./guides/agent-design-guide.md)

---

## Contact & Support

**Workshop Delivery:** Contact Centric Consulting for facilitated workshop delivery

**Questions:** Open issues in the GitHub repository

**Contributions:** Pull requests welcome for patterns, agents, and improvements

---

**Last Updated:** April 2, 2026  
**Epic:** #16 - Spring Boot Workshop Adaptation  
**Version:** 1.0
