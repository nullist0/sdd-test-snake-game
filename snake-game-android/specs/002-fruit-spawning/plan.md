# Implementation Plan: Strategic Fruit Spawning

**Branch**: `002-fruit-spawning` | **Date**: 2026-01-18 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-fruit-spawning/spec.md`

## Summary

Implement strategic fruit spawning for the snake game where fruits preferentially spawn in a 3x3 grid centered on the snake's tail, with fallback to random grid placement when the preferred zone is occupied. This adds strategic depth by allowing players to influence fruit spawn locations through tail positioning. The implementation uses Kotlin/Compose for Android with pure domain logic for spawn algorithms and Compose Canvas for visual rendering.

## Technical Context

**Language/Version**: Kotlin 1.9.20 for Android

**Primary Dependencies**:
- Jetpack Compose (BOM 2023.10.01) for UI rendering
- AndroidX Lifecycle & ViewModel (2.7.0) for state management
- Kotlin Coroutines (1.7.3) for asynchronous operations
- Material3 for UI components
- Compose Canvas for grid rendering

**Storage**: N/A (game state held in memory via ViewModel StateFlow)

**Testing**:
- JUnit 4.13.2 for unit tests
- Kotlin Test 1.9.20 for Kotlin-specific test utilities
- Coroutines Test 1.7.3 for flow testing
- Compose UI Test for rendering validation

**Target Platform**: Android SDK 24+ (Android 7.0 Nougat), compileSdk 34

**Project Type**: Mobile (Android single-module application)

**Performance Goals**:
- 60 FPS during fruit spawning and rendering
- <50ms spawn calculation time (from trigger to position determination)
- Instant visual feedback (no perceptible delay between collection and new spawn)

**Constraints**:
- Offline-only (no network required)
- Minimal memory footprint (single Fruit object in state)
- Must handle grids from 10x10 to 30x30 efficiently
- Spawn algorithm must be deterministic given same grid state

**Scale/Scope**:
- Single fruit active at any time
- ~8-12 source files for complete implementation
- Compose Canvas rendering with clean architecture (domain, UI layers)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Feature-First Development ✅
- Feature spec defines user scenarios with testable acceptance criteria (3 user stories)
- Functional requirements are measurable and unambiguous (FR-001 through FR-011)
- Success criteria defined from user and game design perspective (SC-001 through SC-008)
- No implementation details in specification (algorithms, data structures excluded from spec)
**Status**: PASS

### Principle II: User Experience Priority ✅
- Strategic gameplay: tail-centered spawning adds player agency
- Visual clarity: fruit must be immediately distinguishable (FR-010, SC-005)
- Performance target: 60 FPS maintained, <50ms spawn time (SC-003, SC-008)
- Robustness: fallback spawning ensures game never gets stuck (FR-004)
- Boundary handling: graceful edge/corner cases (FR-007, SC-007)
**Status**: PASS

### Principle III: Test-Before-Implementation (NON-NEGOTIABLE) ✅
- TDD workflow will be enforced during implementation phase
- Tests MUST be written before spawn algorithm implementation
- Tests MUST fail initially (proving they test the right behavior)
- Unit tests for spawn zone calculation, random selection, fallback logic
- Integration tests for spawn → state update → rendering flow
- Contract tests between spawn logic and game state
**Status**: PASS (to be enforced in implementation phase)

### Principle IV: Performance & Efficiency ✅
- 60 FPS target explicitly defined (SC-008)
- <50ms spawn calculation requirement (SC-003)
- Memory: single Fruit object, no spawn history tracking
- Algorithm efficiency: 3x3 zone check is O(9), grid fallback is O(grid_size) worst case
- No unnecessary allocations during spawn (reuse existing grid state queries)
**Status**: PASS

### Principle V: Code Simplicity & Maintainability ✅
- YAGNI: implementing only required spawn logic, no multi-fruit or special types
- Clean architecture: spawn logic in domain layer, rendering in UI layer
- Clear naming: SpawnZone, FruitSpawnUseCase, FruitRenderer
- Modular: spawn calculation separate from grid state management
- No over-engineering: straightforward 3x3 area calculation and random selection
**Status**: PASS

### Mobile Development Constraints ✅
- **Minimum SDK**: Android API 24+ (defined in Technical Context)
- **Permissions**: None required (offline game)
- **Lifecycle Management**: Fruit state in ViewModel survives configuration changes
- **Orientation**: Portrait/landscape support via responsive grid rendering
- **Offline Support**: Fully offline, no dependencies
- **Security**: No sensitive data, no external APIs
- **Quality Standards**: Spawn logic deterministic and testable, no race conditions
**Status**: PASS

### Development Workflow ✅
- Specification phase complete (spec.md)
- Planning phase in progress (this file)
- Constitution check passing before Phase 0 research
- Task generation will follow (/speckit.tasks)
- TDD implementation will follow (/speckit.implement)
**Status**: PASS

**Overall Gate Status**: ✅ PASS - All principles satisfied, no violations requiring justification

## Project Structure

### Documentation (this feature)

```text
specs/002-fruit-spawning/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/
├── src/
│   ├── main/
│   │   ├── java/com/snakegame/
│   │   │   ├── domain/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Direction.kt (existing)
│   │   │   │   │   ├── Position.kt (existing)
│   │   │   │   │   ├── Snake.kt (existing)
│   │   │   │   │   ├── Fruit.kt (NEW - for this feature)
│   │   │   │   │   └── SpawnZone.kt (NEW - for this feature)
│   │   │   │   └── usecase/
│   │   │   │       ├── ValidateDirectionUseCase.kt (existing)
│   │   │   │       ├── DetectSwipeDirectionUseCase.kt (existing from 001)
│   │   │   │       ├── CalculateSpawnZoneUseCase.kt (NEW)
│   │   │   │       ├── FindEmptyCellsUseCase.kt (NEW)
│   │   │   │       └── SpawnFruitUseCase.kt (NEW - orchestrates spawn logic)
│   │   │   └── ui/
│   │   │       ├── game/
│   │   │       │   ├── GameViewModel.kt (existing, will be modified)
│   │   │       │   ├── GameState.kt (existing, will be modified)
│   │   │       │   ├── GameScreen.kt (existing from 001)
│   │   │       │   ├── SwipeGestureDetector.kt (existing from 001)
│   │   │       │   └── FruitRenderer.kt (NEW - Compose Canvas rendering)
│   │   │       └── theme/ (existing)
│   │   └── AndroidManifest.xml (existing)
│   └── test/
│       └── java/com/snakegame/
│           ├── domain/
│           │   ├── model/
│           │   │   ├── DirectionTest.kt (existing)
│           │   │   ├── FruitTest.kt (NEW)
│           │   │   └── SpawnZoneTest.kt (NEW)
│           │   └── usecase/
│           │       ├── ValidateDirectionUseCaseTest.kt (existing)
│           │       ├── CalculateSpawnZoneUseCaseTest.kt (NEW)
│           │       ├── FindEmptyCellsUseCaseTest.kt (NEW)
│           │       └── SpawnFruitUseCaseTest.kt (NEW)
│           └── ui/
│               └── game/
│                   ├── GameViewModelTest.kt (existing, will be expanded)
│                   └── FruitRendererTest.kt (NEW)
└── build.gradle.kts (existing, Compose already enabled)
```

**Structure Decision**:
This is an Android single-module mobile application following clean architecture principles established in feature 001. The project maintains separation of concerns:
- **domain/model**: Core data models (Fruit, SpawnZone with Position and Grid references)
- **domain/usecase**: Business logic (spawn zone calculation, empty cell detection, spawn orchestration)
- **ui/game**: Compose UI layer (ViewModel state management, Canvas rendering)

Existing infrastructure from feature 001:
- GameViewModel and GameState structure established
- Position and Snake models available
- Compose setup complete

New components for fruit spawning:
- Fruit model to represent collectible game object
- SpawnZone model to encapsulate 3x3 tail-centered area calculation
- CalculateSpawnZoneUseCase for determining spawn boundaries
- FindEmptyCellsUseCase for querying available positions
- SpawnFruitUseCase for orchestrating preferential → fallback logic
- FruitRenderer for Compose Canvas visualization

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**No violations** - Constitution Check passed all gates.
