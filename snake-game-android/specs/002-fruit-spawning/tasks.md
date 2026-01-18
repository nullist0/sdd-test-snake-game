# Implementation Tasks: Strategic Fruit Spawning

**Feature**: 002-fruit-spawning
**Branch**: `002-fruit-spawning`
**Status**: Ready for Implementation
**Generated**: 2026-01-18

## Overview

This document breaks down the implementation of strategic fruit spawning into dependency-ordered tasks organized by user story. The implementation follows Test-Driven Development (TDD) as mandated by the project constitution.

**Total Tasks**: 22
**User Stories**: 3 (US1: P1, US2: P2, US3: P3)
**Estimated Time**: 2.5-3 hours

## Task Legend

- `[P]` = Parallelizable (can be done simultaneously with other [P] tasks in same phase)
- `[US#]` = User Story number (maps to spec.md user stories)
- **File paths are absolute** from `app/src/main/java/com/snakegame/` or `app/src/test/java/com/snakegame/`

## Implementation Strategy

**MVP Delivery**: User Story 1 (P1) - Nearby Fruit Spawning (3x3 tail-centered)
- Delivers core fruit spawning mechanic
- Independently testable
- Provides strategic gameplay element

**Incremental Delivery**:
1. **Phase 3**: US1 (P1) - 3x3 tail-centered spawning
2. **Phase 4**: US2 (P2) - Fallback random spawning (builds on US1)
3. **Phase 5**: US3 (P3) - Visual fruit appearance polish

---

## Phase 1: Setup & Domain Models (20 min)

**Goal**: Create foundational data models needed by all user stories.

### Tasks

- [ ] T001 [P] Create Fruit data class in domain/model/Fruit.kt
  - Define position: Position field
  - Define isActive: Boolean = true field
  - Add KDoc documentation
  - **No tests needed initially** (simple data class)

- [ ] T002 [P] Extend GameState with fruit property in ui/game/GameState.kt
  - Add fruit: Fruit? field (nullable for no-fruit state)
  - Update initial() factory method to support fruit
  - Update copy() usages if needed
  - **Tests**: Extend existing GameStateTest if present

---

## Phase 2: Foundational Use Cases (40 min)

**Goal**: Implement core spawn logic building blocks used by all user stories.

**Blocking**: These tasks must complete before any user story implementation begins.

### Tasks

#### CalculateSpawnZoneUseCase (TDD)

- [ ] T003 [P] Write tests for CalculateSpawnZoneUseCase in domain/usecase/CalculateSpawnZoneUseCaseTest.kt
  - Test center position (5,5) returns 9 cells in 3x3 grid
  - Test corner position (0,0) returns 4 cells (2x2, clipped to bounds)
  - Test edge position (0,5) returns 6 cells (2x3, clipped)
  - Test single-cell grid (1x1) returns 1 cell
  - Test all returned positions are within grid bounds
  - Test positions include tail position and 8 adjacent cells
  - **Expected**: Tests FAIL (Red)

- [ ] T004 Implement CalculateSpawnZoneUseCase in domain/usecase/CalculateSpawnZoneUseCase.kt
  - Constructor: no dependencies needed
  - Operator fun invoke(tailPosition: Position, gridWidth: Int, gridHeight: Int): List<Position>
  - Calculate 3x3 area: tail ± 1 in both X and Y
  - Filter out positions outside grid bounds (x in 0 until gridWidth && y in 0 until gridHeight)
  - Return list of valid positions
  - **Expected**: Tests PASS (Green)

#### FindEmptyCellsUseCase (TDD)

- [ ] T005 [P] Write tests for FindEmptyCellsUseCase in domain/usecase/FindEmptyCellsUseCaseTest.kt
  - Test empty grid returns all candidate positions
  - Test snake-occupied cells are filtered out
  - Test fruit-occupied cell is filtered out
  - Test mixed occupancy (some empty, some occupied)
  - Test all-occupied candidates returns empty list
  - Test no fruit (null) only filters snake segments
  - **Expected**: Tests FAIL (Red)

- [ ] T006 Implement FindEmptyCellsUseCase in domain/usecase/FindEmptyCellsUseCase.kt
  - Constructor: no dependencies needed
  - Operator fun invoke(candidates: List<Position>, snakeSegments: List<Position>, existingFruit: Fruit?): List<Position>
  - Create set of occupied positions (snakeSegments + fruit position if present)
  - Filter candidates to exclude occupied positions
  - Return filtered list
  - **Expected**: Tests PASS (Green)

---

## Phase 3: User Story 1 (P1) - Nearby Fruit Spawning

**Priority**: P1 (Critical - MVP)
**Goal**: Fruits spawn in 3x3 grid centered on snake's tail when space available

**Acceptance Criteria** (from spec.md):
1. Snake tail at (5,5) with empty spaces → fruit in 8 surrounding cells
2. Multiple empty cells in 3x3 → fruit in one random empty cell
3. Only 2 empty cells in 3x3 → fruit in one of those 2 cells
4. Game start with short snake → fruit in 3x3 tail area

**Independent Test**: Observe fruit spawn locations relative to tail over multiple spawn events.

### Tasks

#### 3.1 SpawnFruitUseCase - Preferred Zone Logic (TDD) - 35 min

- [ ] T007 [P] [US1] Write tests for SpawnFruitUseCase (preferred zone only) in domain/usecase/SpawnFruitUseCaseTest.kt
  - Test spawns in 3x3 zone when empty cells available
  - Test random selection from multiple empty cells in zone
  - Test deterministic spawn with fixed Random seed
  - Test spawns in one of limited empty cells (e.g., 2 out of 9)
  - **Note**: Fallback tests added in Phase 4
  - **Expected**: Tests FAIL (Red)

- [ ] T008 [US1] Implement SpawnFruitUseCase (preferred zone tier only) in domain/usecase/SpawnFruitUseCase.kt
  - Constructor: inject CalculateSpawnZoneUseCase, FindEmptyCellsUseCase
  - Operator fun invoke(gameState: GameState): Position
  - Calculate spawn zone using tail position and grid dimensions
  - Find empty cells in zone using snake segments and existing fruit
  - If empty cells exist: return random cell
  - Else: throw IllegalStateException (fallback added in Phase 4)
  - **Expected**: Tests PASS (Green)

#### 3.2 GameViewModel Integration (TDD) - 30 min

- [ ] T009 [US1] Write integration tests for fruit spawning in GameViewModel
in ui/game/GameViewModelTest.kt
  - Test initial fruit spawns on game start
  - Test fruit spawns in 3x3 tail area
  - Test fruit position is within spawn zone
  - Test fruit does not spawn on snake segments
  - **Expected**: Tests FAIL (Red)

- [ ] T010 [US1] Integrate fruit spawning into GameViewModel in ui/game/GameViewModel.kt
  - Add SpawnFruitUseCase as constructor dependency
  - Modify initial game state to spawn first fruit
  - Add fruit spawning trigger (called after fruit collection)
  - Update GameState with spawned fruit
  - **Expected**: Tests PASS (Green)

#### 3.3 User Story 1 Validation

- [ ] T011 [US1] Run all US1 tests and verify passing
  - CalculateSpawnZoneUseCaseTest (all passing)
  - FindEmptyCellsUseCaseTest (all passing)
  - SpawnFruitUseCaseTest (preferred zone tests passing)
  - GameViewModelTest (fruit spawn tests passing)
  - **Expected**: 100% test pass rate for US1

---

## Phase 4: User Story 2 (P2) - Fallback Random Spawning

**Priority**: P2 (High - prevents game from getting stuck)
**Goal**: When 3x3 tail area is full, spawn fruit anywhere on grid

**Acceptance Criteria** (from spec.md):
1. Snake fills entire 3x3 area → fruit spawns elsewhere on grid
2. Tail in corner with all 3x3 occupied → fruit in random empty location
3. Snake fills most of grid, 5 empty cells outside 3x3 → fruit in one of those 5
4. 3x3 blocked but many empty cells → equal probability anywhere

**Independent Test**: Create scenarios where snake fills 3x3 tail area, verify fruit spawns elsewhere.

**Dependencies**: US1 must be complete (requires preferred zone logic working)

### Tasks

#### 4.1 Fallback Logic (TDD) - 25 min

- [ ] T012 [P] [US2] Extend SpawnFruitUseCaseTest with fallback scenarios
  - Test fallback to grid when 3x3 zone fully occupied
  - Test fallback when only tail zone blocked (many empty cells elsewhere)
  - Test exception when no empty cells anywhere on grid
  - Test uniform distribution across grid in fallback mode (statistical)
  - **Expected**: Tests FAIL (Red)

- [ ] T013 [US2] Implement fallback logic in SpawnFruitUseCase
  - After preferred zone check fails (no empty cells in zone):
  - Generate all grid positions (0 until width × 0 until height)
  - Find empty cells in entire grid
  - If empty cells exist: return random cell from grid
  - If no empty cells: throw IllegalStateException("No empty cells - game over")
  - **Expected**: Tests PASS (Green)

#### 4.2 User Story 2 Validation

- [ ] T014 [US2] Manual testing of US2 acceptance criteria
  - Create test scenario: snake surrounds tail (3x3 full)
  - Verify fruit spawns outside 3x3 area
  - Create scenario: tail in corner with all adjacent cells blocked
  - Verify fruit spawns in empty grid location
  - **Expected**: All 4 acceptance criteria pass

---

## Phase 5: User Story 3 (P3) - Visual Fruit Appearance

**Priority**: P3 (Nice-to-have - polish and clarity)
**Goal**: Fruit is visually distinct and immediately identifiable on screen

**Acceptance Criteria** (from spec.md):
1. Fruit visible with distinct color/shape from snake and background
2. Visual transition is instant (no delay between collection and new spawn)
3. Fruit remains consistently visible without flickering
4. Fruit appearance consistent across all spawn locations

**Independent Test**: Spawn multiple fruits and verify each is immediately visible and distinguishable.

**Dependencies**: US1 must be complete (requires fruit position in state)

### Tasks

#### 5.1 Fruit Rendering (TDD) - 35 min

- [ ] T015 [P] [US3] Create FruitRenderer composable in ui/game/FruitRenderer.kt
  - @Composable fun FruitRenderer(fruit: Fruit?, cellSize: Dp, modifier: Modifier)
  - If fruit is non-null and active: render using Canvas
  - Draw circle shape (40% of cell size, red color)
  - Center circle within grid cell at fruit position
  - **Compose UI test**: Verify fruit renders when present

- [ ] T016 [US3] Integrate FruitRenderer into GameScreen in ui/game/GameScreen.kt
  - Add FruitRenderer to GameScreen composition
  - Pass gameState.fruit and calculated cellSize
  - Layer fruit renderer above/below snake renderer appropriately
  - **Compose UI test**: Verify fruit and snake both render correctly

#### 5.2 User Story 3 Validation

- [ ] T017 [US3] Visual validation of fruit appearance
  - Build and run on device/emulator
  - Verify fruit is red circle, distinct from snake
  - Collect fruit, verify new fruit appears instantly
  - Move snake around, verify fruit remains visible without flicker
  - Spawn fruit in different locations, verify consistent appearance
  - **Expected**: All 4 acceptance criteria met

---

## Phase 6: Polish & Integration

**Goal**: Final integration, testing, and quality assurance

### Tasks

#### 6.1 Fruit Collection Logic

- [ ] T018 [P] Implement fruit collection in GameViewModel
  - Add collision detection between snake head and fruit position
  - On collision: clear current fruit, grow snake, spawn new fruit
  - Update GameState reactively via StateFlow
  - **Test**: Verify fruit collection → respawn cycle

#### 6.2 Integration & Regression Testing

- [ ] T019 [P] Run full test suite
  - Execute: `./gradlew test`
  - Verify all unit tests pass (domain models, use cases)
  - Verify no regressions in existing tests (Direction, ValidateDirection, Swipe)
  - **Expected**: 100% pass rate

- [ ] T020 [P] Run Android instrumentation tests
  - Execute: `./gradlew connectedAndroidTest`
  - Verify Compose UI tests pass (FruitRenderer, GameScreen integration)
  - **Expected**: All instrumentation tests pass

#### 6.3 Code Quality & Documentation

- [ ] T021 Verify Constitution compliance
  - Review implementation against Constitution Principle III (TDD)
  - Confirm tests written before implementation (Red-Green-Refactor)
  - Verify spawn algorithm matches research decisions
  - **Expected**: Full compliance

- [ ] T022 Final manual test of all user stories
  - Run through all acceptance criteria for US1, US2, US3
  - Test on different grid sizes (10x10, 20x20, 30x30)
  - Verify <50ms spawn time (use Android Profiler if needed)
  - Test edge cases: full grid, minimal snake, corner tail positions
  - **Expected**: All user stories deliver specified value

---

## Dependency Graph

### User Story Completion Order

```
Phase 1: Setup (Models)
    ↓
Phase 2: Foundational Use Cases (blocks all user stories)
    ↓
Phase 3: User Story 1 (P1) ← MVP DELIVERY POINT
    ↓
Phase 4: User Story 2 (P2) ← depends on US1 (extends spawn logic)
    ↓
Phase 5: User Story 3 (P3) ← depends on US1 (requires fruit in state)
    ↓
Phase 6: Polish
```

### Task Dependencies Within User Stories

**User Story 1** (can work independently after Phase 2):
```
T007 (Tests) ─→ T008 (SpawnFruitUseCase preferred zone)
T009 (Tests) ─→ T010 (GameViewModel integration)
    ↓
T011 (Validation)
```

**User Story 2** (requires US1 complete):
```
US1 Complete ─→ T012 (Fallback tests) ─→ T013 (Fallback implementation) ─→ T014 (Manual testing)
```

**User Story 3** (requires US1 complete):
```
US1 Complete ─→ T015 (FruitRenderer) + T016 (GameScreen integration) ─→ T017 (Visual validation)
```

---

## Parallel Execution Opportunities

### Phase 1: Setup
- T001, T002: Can run in parallel (different files)

### Phase 2: Foundational Use Cases
- T003, T005: Can run in parallel (different test files)
- T004, T006: Sequential after their respective tests

### Phase 3: User Story 1
- T007, T009: Can run in parallel (different test files)
- T008, T010: Sequential after their respective tests

### Phase 4: User Story 2
- T012: Single task (extends existing test file)
- T013: Sequential after T012

### Phase 5: User Story 3
- T015: Single focused task
- T016: Sequential (depends on T015)

### Phase 6: Polish
- T018, T019, T020: Can run in parallel (different concerns)
- T021, T022: Sequential (validation tasks)

**Maximum parallelization**: Up to 2-3 developers in Phases 1-2, 2 developers in Phase 3

---

## Testing Summary

### Test Coverage by User Story

**User Story 1** (P1):
- CalculateSpawnZoneUseCaseTest: 6 test cases
- FindEmptyCellsUseCaseTest: 6 test cases
- SpawnFruitUseCaseTest (preferred): 4 test cases
- GameViewModelTest (fruit spawn): 4 integration test cases
- **Total**: 20 automated tests + 4 manual acceptance criteria

**User Story 2** (P2):
- SpawnFruitUseCaseTest (fallback): 4 test cases
- **Total**: 4 automated tests + 4 manual acceptance criteria

**User Story 3** (P3):
- FruitRenderer: 1 Compose UI test
- GameScreen integration: 1 Compose UI test
- **Total**: 2 automated tests + 4 manual acceptance criteria

**Grand Total**: 26 automated tests + 12 manual acceptance criteria

---

## Implementation Checklist

### Pre-Implementation
- [x] Constitution Check passed (plan.md)
- [x] All design documents generated (plan, research, data-model, contracts, quickstart)
- [x] User stories prioritized (P1, P2, P3)
- [ ] Development environment ready (build fix from feature 001 if needed)

### During Implementation
- [ ] Follow TDD strictly: Red → Green → Refactor
- [ ] Write tests before implementation for every component
- [ ] Verify tests FAIL before implementing (Red)
- [ ] Implement minimum code to pass tests (Green)
- [ ] Refactor for quality while keeping tests green
- [ ] Commit after each completed task

### Post-Implementation
- [ ] All automated tests passing
- [ ] All manual acceptance criteria validated
- [ ] Performance validated (<50ms spawn time)
- [ ] Code reviewed
- [ ] No constitution violations
- [ ] Documentation updated

---

## Quick Reference

**File Paths**:
- Domain Models: `app/src/main/java/com/snakegame/domain/model/`
- Domain Use Cases: `app/src/main/java/com/snakegame/domain/usecase/`
- UI Components: `app/src/main/java/com/snakegame/ui/game/`
- Unit Tests: `app/src/test/java/com/snakegame/`
- Android Tests: `app/src/androidTest/java/com/snakegame/`

**Key Commands**:
- Run unit tests: `./gradlew test`
- Run Android tests: `./gradlew connectedAndroidTest`
- Build debug: `./gradlew assembleDebug`
- Install on device: `./gradlew installDebug`

**Success Criteria**:
- ✅ 95% of fruit spawns in 3x3 tail area when available (SC-001)
- ✅ 100% fallback use when tail area blocked (SC-002)
- ✅ <50ms spawn time (SC-003)
- ✅ Zero fruit spawns on occupied cells (SC-004)
- ✅ Immediate fruit visibility (SC-005)
- ✅ Uniform random distribution (SC-006)
- ✅ 60 FPS performance maintained (SC-008)

---

**Ready to implement!** Start with Phase 1, Task T001.
