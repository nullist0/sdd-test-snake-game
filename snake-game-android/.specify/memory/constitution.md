<!--
Sync Impact Report - Constitution Update
Version: 1.0.0 (Initial ratification)
Date: 2026-01-18

Changes:
- Initial constitution created for Snake Game Android project
- Established 5 core principles for mobile game development
- Added mobile-specific constraints and development workflow
- Defined governance structure with version 1.0.0

Principles Defined:
- I. Feature-First Development
- II. User Experience Priority
- III. Test-Before-Implementation (NON-NEGOTIABLE)
- IV. Performance & Efficiency
- V. Code Simplicity & Maintainability

Templates Validated:
✅ .specify/templates/spec-template.md - Aligned with user scenarios and requirements sections
✅ .specify/templates/plan-template.md - Constitution Check section references this file
✅ .specify/templates/tasks-template.md - Task categorization aligns with principle-driven approach
✅ .specify/templates/checklist-template.md - Quality gates support test-first principle
✅ .specify/templates/agent-file-template.md - Generic guidance (no agent-specific updates needed)

Command Files Validated:
✅ .claude/commands/speckit.specify.md - References constitution compliance
✅ .claude/commands/speckit.plan.md - Constitution check integrated
✅ .claude/commands/speckit.tasks.md - Task structure supports principles
✅ .claude/commands/speckit.checklist.md - Quality validation aligns
✅ .claude/commands/speckit.constitution.md - This command file

Follow-up TODOs:
- None (all placeholders filled)
-->

# Snake Game Android Constitution

## Core Principles

### I. Feature-First Development

Every feature must start with a clear specification that defines:
- User scenarios with testable acceptance criteria
- Functional requirements that are measurable and unambiguous
- Success criteria from user and business perspective
- No implementation details in specifications (languages, frameworks, APIs excluded)

**Rationale**: Separating WHAT from HOW ensures features solve real user needs before committing to implementation approaches. This prevents over-engineering and keeps development focused on delivering value.

### II. User Experience Priority

All features must prioritize user experience:
- Intuitive touch controls optimized for mobile gameplay
- Responsive UI with consistent 60 FPS target for game rendering
- Clear visual feedback for all game actions and state changes
- Graceful error handling with user-friendly messages
- Support for different screen sizes and orientations where applicable

**Rationale**: Mobile games live or die by user experience. Smooth gameplay, intuitive controls, and responsive UI are non-negotiable for player retention and satisfaction.

### III. Test-Before-Implementation (NON-NEGOTIABLE)

Test-Driven Development is mandatory for all features:
- Tests MUST be written before implementation
- Tests MUST fail initially (proving they test the right thing)
- Implementation proceeds only after test approval
- Red-Green-Refactor cycle strictly enforced
- Focus on integration tests for game mechanics, contract tests for component interfaces

**Rationale**: TDD ensures code correctness, prevents regressions, and serves as living documentation. For game development, this is critical for maintaining consistent game logic as features evolve.

### IV. Performance & Efficiency

Mobile resource constraints require disciplined performance management:
- Target 60 FPS for game rendering and animations
- Memory usage must stay within Android platform guidelines
- Battery consumption must be monitored and optimized
- Asset loading must be efficient (lazy loading, compression)
- Network calls (if any) must be asynchronous and resilient

**Rationale**: Mobile devices have limited CPU, memory, and battery. Poor performance leads to poor reviews, uninstalls, and user frustration. Performance must be a first-class concern, not an afterthought.

### V. Code Simplicity & Maintainability

Start simple and evolve based on actual needs:
- YAGNI principle: implement only what is currently required
- Clear naming conventions for game entities, components, and systems
- Modular architecture with clear separation of concerns
- Game logic separated from rendering and UI
- Avoid premature optimization and over-abstraction

**Rationale**: Simple code is easier to understand, test, and modify. Game development often involves frequent iteration and experimentation. Complex architectures make rapid iteration costly.

## Mobile Development Constraints

All features must comply with Android platform requirements:
- **Minimum SDK**: Target Android API level to be defined per feature (document in Technical Context)
- **Permissions**: Request only necessary permissions with clear user benefit explanation
- **Lifecycle Management**: Properly handle activity lifecycle (pause, resume, destroy)
- **Orientation**: Define orientation support (portrait, landscape, both) in feature spec
- **Asset Management**: Optimize assets for mobile (compressed textures, appropriate resolutions)
- **Offline Support**: Core gameplay must work offline unless feature explicitly requires network

**Security Requirements**:
- No hardcoded secrets or API keys in source code
- Secure storage for user data and preferences
- Input validation for all user-provided data
- Safe handling of external data sources (if applicable)

**Quality Standards**:
- No crashes or ANRs (Application Not Responding) in production code
- Memory leaks must be detected and fixed before merge
- Lint warnings must be addressed (no suppressions without justification)

## Development Workflow

### Feature Development Process

1. **Specification Phase** (`/speckit.specify`):
   - Write feature spec focusing on user value and requirements
   - No implementation details
   - Pass specification quality checklist

2. **Clarification Phase** (`/speckit.clarify`) - Optional:
   - Resolve any ambiguous requirements
   - Maximum 3 clarification questions per feature
   - Update spec with answers

3. **Planning Phase** (`/speckit.plan`):
   - Research existing codebase patterns
   - Design technical approach
   - Pass constitution compliance check
   - Document complexity justifications if needed

4. **Task Generation** (`/speckit.tasks`):
   - Break down implementation into dependency-ordered tasks
   - Organize by user story for independent delivery
   - Mark parallel opportunities

5. **Implementation Phase** (`/speckit.implement`):
   - Write tests first (Red)
   - Implement to pass tests (Green)
   - Refactor for quality (Refactor)
   - Commit frequently with clear messages

6. **Quality Validation**:
   - All tests passing
   - Code review completed
   - Performance benchmarks met
   - No memory leaks detected

### Code Review Requirements

All pull requests must:
- Include test coverage for new functionality
- Pass all automated tests and lint checks
- Demonstrate feature working as specified
- Include performance metrics if applicable
- Address all reviewer feedback before merge

### Compliance and Versioning

- Constitution supersedes all other development practices
- Amendments require:
  - Clear documentation of change rationale
  - Update to dependent templates and command files
  - Version bump following semantic versioning
  - Approval before propagation
- Constitution violations require explicit justification in Complexity Tracking section of plan.md
- Regular reviews to ensure constitution remains aligned with project evolution

## Governance

**Constitution Authority**: This constitution is the highest-level governance document for the Snake Game Android project. All features, implementations, and processes must comply with these principles.

**Amendment Procedure**:
1. Proposed changes documented with rationale
2. Impact analysis on existing features and templates
3. Review and approval process
4. Version increment (MAJOR for breaking changes, MINOR for additions, PATCH for clarifications)
5. Propagation to all dependent artifacts (templates, commands, guidance files)

**Version Policy**:
- **MAJOR** (X.0.0): Backward-incompatible principle removals, redefinitions, or scope changes
- **MINOR** (x.Y.0): New principles added, existing principles materially expanded
- **PATCH** (x.y.Z): Clarifications, wording improvements, typo fixes

**Compliance Review**:
- All feature plans must pass Constitution Check (gate in plan-template.md)
- Constitution violations flagged in Complexity Tracking section require justification
- Regular audits to ensure ongoing compliance

**Guidance**: Development guidance is auto-generated from feature plans and stored in agent-specific guidance files. Constitution principles remain constant; guidance evolves with project needs.

**Version**: 1.0.0 | **Ratified**: 2026-01-18 | **Last Amended**: 2026-01-18
