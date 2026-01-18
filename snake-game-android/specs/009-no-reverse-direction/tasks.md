# Implementation Tasks: No Reverse Direction Control

**Feature**: 009-no-reverse-direction
**Branch**: `009-no-reverse-direction`
**Generated**: 2026-01-18
**Approach**: Test-Driven Development (TDD) - Tests written before implementation

## Task Summary

**Total Tasks**: 17
**User Stories**: 3 (P1: Reverse Prevention, P2: Perpendicular Control, P3: Same Direction)
**Parallel Opportunities**: 8 parallelizable tasks marked with [P]
**TDD Approach**: All tests written before implementation (RED → GREEN → REFACTOR)

## User Story Breakdown

| Story | Priority | Tasks | Independent Test Criteria |
|-------|----------|-------|---------------------------|
| US1: Reverse Direction Prevention | P1 (MVP) | 7 | Reverse direction inputs rejected 100% of time for all 4 direction pairs |
| US2: Perpendicular Direction Control | P2 | 4 | Perpendicular direction inputs accepted 100% of time for all 8 combinations |
| US3: Same Direction Handling | P3 | 2 | Same direction inputs maintain current direction without errors |

**MVP Scope**: User Story 1 (US1) delivers core safety mechanic - prevents instant self-collision

## Implementation Strategy

### Development Phases

1. **Phase 1: Setup** (T001-T002) - Project initialization
2. **Phase 2: Foundational** (T003-T004) - Shared models needed by all stories
3. **Phase 3: User Story 1** (T005-T011) - Reverse direction prevention (MVP)
4. **Phase 4: User Story 2** (T012-T015) - Perpendicular control validation
5. **Phase 5: User Story 3** (T016-T017) - Same direction handling
6. **Phase 6: Polish** - Cross-cutting concerns (none for this feature)

### TDD Workflow Per User Story

Each user story follows strict TDD cycle:
1. **RED**: Write failing tests for acceptance scenarios
2. **GREEN**: Implement minimum code to pass tests
3. **REFACTOR**: Clean up implementation while keeping tests green

---

## Phase 1: Setup & Project Initialization

**Goal**: Create Android project structure and configure build system

**Prerequisites**: Android Studio installed, Android SDK 24+ configured

### Tasks

- [x] T001 Initialize Android project with Kotlin support in root directory
  - Create project via Android Studio: Empty Activity template
  - Configure: minSdk=24, targetSdk=34, compileSdk=34
  - Package name: com.snakegame
  - Files: `build.gradle.kts` (root), `settings.gradle.kts`, `gradle.properties`

- [x] T002 Configure module build.gradle.kts for testing dependencies in `app/build.gradle.kts`
  - Add JUnit 4/5 dependencies: `testImplementation("junit:junit:4.13.2")`
  - Add Kotlin test: `testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")`
  - Add ViewModel: `implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")`
  - Add Coroutines: `implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0")`
  - Add StateFlow support
  - File: `app/build.gradle.kts`

---

## Phase 2: Foundational - Shared Models

**Goal**: Create core domain models needed across all user stories

**Prerequisites**: Phase 1 complete

**Independent Test**: Models can be instantiated and used in isolation

### Tasks

- [x] T003 [P] Create Position data class in `app/src/main/java/com/snakegame/domain/model/Position.kt`
  - Properties: `x: Int, y: Int`
  - Simple data class, no validation needed
  - File: `app/src/main/java/com/snakegame/domain/model/Position.kt`

- [x] T004 [P] Create Snake data class in `app/src/main/java/com/snakegame/domain/model/Snake.kt`
  - Properties: `head: Position, body: List<Position>, direction: Direction`
  - Companion object: `fun initial(gridSize: Int = 15): Snake`
  - Initial direction: RIGHT, initial length: 4 segments (1 head + 3 body)
  - File: `app/src/main/java/com/snakegame/domain/model/Snake.kt`

---

## Phase 3: User Story 1 - Reverse Direction Prevention (P1 - MVP)

**Goal**: Prevent reverse (180°) direction changes that would cause instant self-collision

**Why MVP**: Core safety mechanic - without this, players can accidentally game over by reversing into body

**Independent Test**:
- Test all 4 reverse pairs: UP→DOWN, DOWN→UP, LEFT→RIGHT, RIGHT→LEFT
- All reverse directions rejected 100% of time
- Snake direction remains unchanged after rejected input

**Acceptance Criteria**:
1. Snake moving upward + downward input → snake continues upward (rejected)
2. Snake moving leftward + rightward input → snake continues leftward (rejected)
3. Snake moving downward + upward input → snake continues downward (rejected)
4. Snake moving rightward + leftward input → snake continues rightward (rejected)

### Tasks (TDD Order)

#### RED: Write Failing Tests

- [X] T005 [P] [US1] Write Direction enum tests in `app/src/test/java/com/snakegame/domain/model/DirectionTest.kt`
  - Test `reverse()` returns opposite for all 4 directions
  - Test `isReverse(other)` returns true for opposites, false otherwise
  - Test `isPerpendicular(other)` returns false for reverse pairs
  - File: `app/src/test/java/com/snakegame/domain/model/DirectionTest.kt`
  - **Status**: COMPLETE - 7 test methods covering all direction relationships

- [X] T006 [P] [US1] Write parameterized validation tests in `app/src/test/java/com/snakegame/domain/usecase/ValidateDirectionUseCaseTest.kt`
  - Use JUnit `@RunWith(Parameterized::class)`
  - Test all 16 combinations (4 current × 4 requested)
  - Expected results: 4 reverse = false, 12 others = true (perpendicular + same)
  - File: `app/src/test/java/com/snakegame/domain/usecase/ValidateDirectionUseCaseTest.kt`
  - **Status**: COMPLETE - Parameterized tests with 16 combinations + additional explicit perpendicular tests

- [X] T007 [US1] Write ViewModel integration tests in `app/src/test/java/com/snakegame/ui/game/GameViewModelTest.kt`
  - Test initial snake direction is RIGHT
  - Test `handleDirectionInput(LEFT)` keeps direction RIGHT (reverse rejected)
  - Test perpendicular inputs accepted (tested in US2, but reverse rejection here)
  - File: `app/src/test/java/com/snakegame/ui/game/GameViewModelTest.kt`
  - **Status**: COMPLETE - 13 test methods covering all user stories

#### GREEN: Implement to Pass Tests

- [x] T008 [P] [US1] Implement Direction enum in `app/src/main/java/com/snakegame/domain/model/Direction.kt`
  - Enum values: UP, DOWN, LEFT, RIGHT
  - Method: `fun reverse(): Direction` using when expression
  - Method: `fun isReverse(other: Direction): Boolean = this.reverse() == other`
  - Method: `fun isPerpendicular(other: Direction): Boolean = this != other && !isReverse(other)`
  - File: `app/src/main/java/com/snakegame/domain/model/Direction.kt`
  - **Expected**: T005 tests PASS

- [x] T009 [P] [US1] Implement ValidateDirectionUseCase in `app/src/main/java/com/snakegame/domain/usecase/ValidateDirectionUseCase.kt`
  - Class with `operator fun invoke(current: Direction, requested: Direction): Boolean`
  - Logic: `return !current.isReverse(requested)` (reject reverse, accept all others)
  - Add KDoc comments explaining reverse prevention rule
  - File: `app/src/main/java/com/snakegame/domain/usecase/ValidateDirectionUseCase.kt`
  - **Expected**: T006 tests PASS (all 16 combinations validated correctly)

- [x] T010 [US1] Create GameState data class in `app/src/main/java/com/snakegame/ui/game/GameViewModel.kt`
  - Data class: `GameState(snake: Snake, score: Int, isGameOver: Boolean)`
  - Companion: `fun initial(gridSize: Int = 15): GameState`
  - File: `app/src/main/java/com/snakegame/ui/game/GameViewModel.kt` (same file as ViewModel)

- [x] T011 [US1] Implement GameViewModel with direction validation in `app/src/main/java/com/snakegame/ui/game/GameViewModel.kt`
  - Extend `ViewModel()`
  - Property: `private val validateDirection = ValidateDirectionUseCase()`
  - StateFlow: `private val _gameState = MutableStateFlow(GameState.initial())`
  - Public: `val gameState: StateFlow<GameState> = _gameState.asStateFlow()`
  - Method: `fun handleDirectionInput(requested: Direction)` with validation logic
  - If valid: update snake direction via `_gameState.update { state.copy(snake = snake.copy(direction = requested)) }`
  - If invalid: silently ignore (no state change)
  - File: `app/src/main/java/com/snakegame/ui/game/GameViewModel.kt`
  - **Expected**: T007 tests PASS (reverse directions rejected, state unchanged)

#### REFACTOR: Clean Up (If Needed)

No refactoring needed for this simple implementation - code is already minimal and clear.

**Story 1 Completion Check**: ✅ All T005-T011 tests passing, reverse directions rejected 100% of time

---

## Phase 4: User Story 2 - Perpendicular Direction Control (P2)

**Goal**: Validate that perpendicular (90°) direction changes work correctly

**Why P2**: Ensures complete directional control while maintaining safety. Verifies that blocking reverse doesn't accidentally block valid turns.

**Independent Test**:
- Test all 8 perpendicular combinations
- All perpendicular directions accepted 100% of time
- Snake direction updates correctly on perpendicular input

**Acceptance Criteria**:
1. Snake moving upward + left/right input → snake turns left/right
2. Snake moving downward + left/right input → snake turns left/right
3. Snake moving leftward + up/down input → snake turns up/down
4. Snake moving rightward + up/down input → snake turns up/down

### Tasks (TDD Order)

#### RED: Write Failing Tests

- [X] T012 [P] [US2] Add perpendicular validation tests in `app/src/test/java/com/snakegame/domain/usecase/ValidateDirectionUseCaseTest.kt`
  - Test all 8 perpendicular combinations explicitly: UP→LEFT, UP→RIGHT, DOWN→LEFT, DOWN→RIGHT, LEFT→UP, LEFT→DOWN, RIGHT→UP, RIGHT→DOWN
  - Expected: all return `true`
  - File: `app/src/test/java/com/snakegame/domain/usecase/ValidateDirectionUseCaseTest.kt` (extend existing test)
  - **Status**: COMPLETE - ValidateDirectionPerpendicularTest class with 4 explicit test methods

- [X] T013 [US2] Add ViewModel perpendicular tests in `app/src/test/java/com/snakegame/ui/game/GameViewModelTest.kt`
  - Test sequence: RIGHT → UP (perpendicular, should update to UP)
  - Test sequence: UP → LEFT (perpendicular, should update to LEFT)
  - Test sequence: LEFT → DOWN (perpendicular, should update to DOWN)
  - Verify snake.direction changes correctly for each
  - File: `app/src/test/java/com/snakegame/ui/game/GameViewModelTest.kt` (extend existing test)
  - **Status**: COMPLETE - 3 test methods for perpendicular control (lines 82-113)

#### GREEN: Verify Implementation

- [X] T014 [P] [US2] Verify Direction.isPerpendicular() covers all 8 cases in `app/src/main/java/com/snakegame/domain/model/Direction.kt`
  - Review existing implementation: `this != other && !isReverse(other)`
  - Should already handle perpendicular correctly (not same, not reverse = perpendicular)
  - Add code comment documenting perpendicular logic
  - File: `app/src/main/java/com/snakegame/domain/model/Direction.kt`
  - **Status**: COMPLETE - Implementation verified, logic correct

- [X] T015 [US2] Verify ValidateDirectionUseCase accepts perpendicular in `app/src/main/java/com/snakegame/domain/usecase/ValidateDirectionUseCase.kt`
  - Review logic: `!current.isReverse(requested)` already accepts perpendicular
  - Add code comment: "Accepts perpendicular and same directions"
  - File: `app/src/main/java/com/snakegame/domain/usecase/ValidateDirectionUseCase.kt`
  - **Status**: COMPLETE - Implementation verified, logic correct

**Story 2 Completion Check**: ✅ All perpendicular tests passing, 8/8 combinations accepted

---

## Phase 5: User Story 3 - Same Direction Handling (P3)

**Goal**: Verify that same direction input is handled gracefully without errors

**Why P3**: Polish - prevents edge case issues where repeatedly swiping same direction could cause errors

**Independent Test**:
- Test all 4 same-direction cases: UP→UP, DOWN→DOWN, LEFT→LEFT, RIGHT→RIGHT
- All same directions accepted (return true)
- Snake direction remains unchanged (already correct direction)
- No errors or performance issues with rapid same-direction input

**Acceptance Criteria**:
1. Snake moving upward + upward input → continues upward without interruption
2. Snake moving leftward + leftward input → continues leftward without interruption
3. Rapidly inputting same direction → maintains steady movement speed
4. Alternating perpendicular and same direction → handles both correctly

### Tasks (TDD Order)

#### RED: Write Failing Tests

- [X] T016 [P] [US3] Add same-direction tests in `app/src/test/java/com/snakegame/ui/game/GameViewModelTest.kt`
  - Test: initial direction RIGHT, input RIGHT → remains RIGHT
  - Test: change to UP, input UP repeatedly (10x) → remains UP, no errors
  - Test: sequence UP → UP → LEFT → LEFT → DOWN → DOWN (mixed same/perpendicular)
  - File: `app/src/test/java/com/snakegame/ui/game/GameViewModelTest.kt`
  - **Status**: COMPLETE - 3 test methods for same direction handling (lines 116-162)

#### GREEN: Verify Implementation

- [X] T017 [US3] Verify same direction handling in `app/src/main/java/com/snakegame/domain/usecase/ValidateDirectionUseCase.kt`
  - Review: `!current.isReverse(requested)` accepts same direction (not reverse of itself)
  - Add test in DirectionTest.kt: `Direction.UP.isReverse(Direction.UP) == false`
  - Verify ViewModel handles same direction without errors
  - File: `app/src/main/java/com/snakegame/domain/usecase/ValidateDirectionUseCase.kt`
  - **Status**: COMPLETE - Implementation verified, same direction test exists in DirectionTest.kt

**Story 3 Completion Check**: ✅ Same direction inputs handled without errors

---

## Dependency Graph

### User Story Dependencies

```
Setup (T001-T002)
      ↓
Foundational (T003-T004)
      ↓
┌─────┴─────┐
│   US1: Reverse Prevention (T005-T011) [MVP]
│   ↓
│   US2: Perpendicular Control (T012-T015) [Depends on US1]
│   ↓
│   US3: Same Direction Handling (T016-T017) [Depends on US1]
└──────────┘
```

### Task Dependencies

**Blocking Dependencies**:
- T003-T004 must complete before T005-T011 (need Position and Snake models)
- T005-T006 must complete before T008-T009 (TDD: tests before implementation)
- T008-T009 must complete before T010-T011 (ViewModel depends on Direction and UseCase)
- T011 must complete before T012-T015 (US2 extends US1 implementation)
- T011 must complete before T016-T017 (US3 extends US1 implementation)

**Parallel Opportunities**:
- T003 ∥ T004 (Position and Snake are independent)
- T005 ∥ T006 (Direction tests and UseCase tests are independent)
- T008 ∥ T009 (Direction enum and UseCase can be implemented in parallel after tests)
- T012 ∥ T013 (US2 tests can be written in parallel)
- T014 ∥ T015 (US2 verification tasks are independent)

---

## Parallel Execution Examples

### Phase 2: Foundational Models
**Run in parallel** (different files, no dependencies):
```bash
# Developer A:
- T003: Create Position.kt

# Developer B (simultaneously):
- T004: Create Snake.kt
```

### Phase 3: User Story 1 Tests
**Run in parallel** (different test files):
```bash
# Developer A:
- T005: Write DirectionTest.kt

# Developer B (simultaneously):
- T006: Write ValidateDirectionUseCaseTest.kt
```

### Phase 3: User Story 1 Implementation
**Run in parallel** (different files, after tests complete):
```bash
# Developer A:
- T008: Implement Direction.kt

# Developer B (simultaneously):
- T009: Implement ValidateDirectionUseCase.kt
```

### Phase 4: User Story 2
**Run in parallel** (extending different test files):
```bash
# Developer A:
- T012: Add perpendicular tests to ValidateDirectionUseCaseTest.kt

# Developer B (simultaneously):
- T013: Add perpendicular tests to GameViewModelTest.kt
```

---

## Testing Strategy

### Test Coverage Requirements

**Unit Tests** (JUnit, runs in milliseconds):
- ✅ Direction enum: reverse(), isReverse(), isPerpendicular()
- ✅ ValidateDirectionUseCase: all 16 direction combinations
- ✅ GameViewModel: direction validation integration

**Test Metrics**:
- **Target**: 100% coverage for Direction enum and ValidateDirectionUseCase
- **Parameterized Tests**: 16 combinations validated systematically
- **TDD Approach**: Tests written first, then implementation

### Test Execution

```bash
# Run all unit tests
./gradlew test

# Expected output after all tasks:
# DirectionTest: 6 tests PASSED
# ValidateDirectionUseCaseTest: 16 tests PASSED (parameterized)
# GameViewModelTest: 8+ tests PASSED
# Total: 30+ tests, 0 failures
```

---

## Validation Checklist

### Phase 1-2: Setup Complete
- [ ] Android project builds successfully (`./gradlew build`)
- [ ] JUnit dependencies available (check `app/build.gradle.kts`)
- [ ] Package structure created (`com/snakegame/domain/model`, etc.)

### User Story 1 Complete (MVP)
- [ ] All T005-T011 tasks completed
- [ ] All reverse direction tests PASS (4/4 pairs rejected)
- [ ] ViewModel rejects reverse inputs (state unchanged)
- [ ] No crashes or errors when reverse input attempted

### User Story 2 Complete
- [ ] All T012-T015 tasks completed
- [ ] All perpendicular direction tests PASS (8/8 combinations accepted)
- [ ] ViewModel accepts perpendicular inputs (state updates correctly)

### User Story 3 Complete
- [ ] All T016-T017 tasks completed
- [ ] Same direction tests PASS (4/4 cases accepted)
- [ ] Rapid same-direction input handled without errors

### Final Validation
- [ ] All 17 tasks completed
- [ ] All 30+ tests passing (`./gradlew test`)
- [ ] Code follows Kotlin conventions (lint clean)
- [ ] No TODOs or FIXMEs in production code
- [ ] Feature ready for integration with swipe controls (feature 001)

---

## Implementation Notes

### TDD Discipline

**CRITICAL**: Tests MUST be written before implementation. If you skip writing tests first, you violate the constitution's Principle III (Test-Before-Implementation).

**Red-Green-Refactor Cycle**:
1. **RED**: Write test that fails (T005-T007)
2. **GREEN**: Write minimum code to pass test (T008-T011)
3. **REFACTOR**: Clean up code while keeping tests green

### Performance Validation

After T009 (ValidateDirectionUseCase implemented), run microbenchmark:

```kotlin
@Test
fun `validation completes in under 1 millisecond`() {
    val useCase = ValidateDirectionUseCase()
    val iterations = 10000

    val startTime = System.nanoTime()
    repeat(iterations) {
        useCase(Direction.UP, Direction.DOWN)
    }
    val endTime = System.nanoTime()

    val averageMs = (endTime - startTime) / iterations / 1_000_000.0
    assertTrue(averageMs < 1.0, "Average: $averageMs ms")
}
```

**Expected**: ~0.001ms (1 microsecond) per validation

### Integration Points

This feature integrates with:
- **Feature 001 (Swipe Controls)**: Receives Direction from swipe detector
- **Feature 005 (Collision Detection)**: Reverse prevention is first defense, collision detection is second
- **Feature 007 (Snake Movement)**: Validated direction used for movement calculation

These integrations are out of scope for this feature's tasks but should be noted for future work.

---

## Estimated Effort

| Phase | Tasks | Estimated Time |
|-------|-------|----------------|
| Phase 1: Setup | T001-T002 | 30 minutes |
| Phase 2: Foundational | T003-T004 | 15 minutes |
| Phase 3: US1 (MVP) | T005-T011 | 2-3 hours |
| Phase 4: US2 | T012-T015 | 30 minutes |
| Phase 5: US3 | T016-T017 | 15 minutes |
| **Total** | 17 tasks | **3.5-4.5 hours** |

**MVP Only** (US1): ~3 hours
**Full Feature** (US1+US2+US3): ~4 hours

---

## Success Criteria

### Functional Success
- [ ] All 4 reverse direction pairs rejected (UP↔DOWN, LEFT↔RIGHT)
- [ ] All 8 perpendicular combinations accepted
- [ ] All 4 same-direction inputs handled gracefully
- [ ] ViewModel state updates correctly for valid inputs
- [ ] ViewModel state unchanged for invalid inputs

### Technical Success
- [ ] All 30+ tests passing
- [ ] Code coverage >90% for Direction and ValidateDirectionUseCase
- [ ] Validation latency <1ms (microbenchmark confirms)
- [ ] No lint warnings or errors
- [ ] TDD approach followed (tests written first)

### Constitution Compliance
- [ ] ✅ Principle III: Tests written before implementation
- [ ] ✅ Principle IV: Validation <1ms, minimal memory overhead
- [ ] ✅ Principle V: Simple enum-based solution, no over-engineering

**Feature Complete**: All tasks done, all tests passing, ready for `/speckit.implement`
