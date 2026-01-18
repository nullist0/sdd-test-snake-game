# Implementation Tasks: Swipe-Based Snake Controls

**Feature**: 001-swipe-controls
**Branch**: `001-swipe-controls`
**Status**: Ready for Implementation
**Generated**: 2026-01-18

## Overview

This document breaks down the implementation of swipe-based snake controls into dependency-ordered tasks organized by user story. The implementation follows Test-Driven Development (TDD) as mandated by the project constitution (Principle III: NON-NEGOTIABLE).

**Total Tasks**: 25
**User Stories**: 3 (US1: P1, US2: P2, US3: P3)
**Estimated Time**: 3.5-4 hours

## Task Legend

- `[P]` = Parallelizable (can be done simultaneously with other [P] tasks in same phase)
- `[US#]` = User Story number (maps to spec.md user stories)
- **File paths are absolute** from `app/src/main/java/com/snakegame/` or `app/src/test/java/com/snakegame/`

## Implementation Strategy

**MVP Delivery**: User Story 1 (P1) - Directional Control via Swipe
- Delivers core gameplay functionality
- Independently testable
- Provides immediate user value

**Incremental Delivery**:
1. **Phase 3**: US1 (P1) - Basic swipe controls
2. **Phase 4**: US2 (P2) - Reverse direction prevention (builds on US1)
3. **Phase 5**: US3 (P3) - Responsive swipe detection polish

---

## Phase 1: Setup & Configuration (5 min)

**Goal**: Initialize project configuration and constants for swipe gesture detection.

### Tasks

- [X] T001 Create SwipeGestureConfig in domain/model/SwipeGestureConfig.kt
  - Define MIN_SWIPE_DISTANCE = 50.dp
  - Define DEBOUNCE_TIME_MS = 100L
  - Define MAX_QUEUED_DIRECTIONS = 0 (start simple)
  - **No tests needed** (configuration object)

---

## Phase 2: Foundational Models (20 min)

**Goal**: Implement core data models needed by all user stories.

**Blocking**: These tasks must complete before any user story implementation begins.

### Tasks

#### SwipeGesture Model (TDD)

- [X] T002 [P] Write tests for SwipeGesture in domain/model/SwipeGestureTest.kt
  - Test delta calculation (endPosition - startPosition)
  - Test getDistance() for euclidean distance
  - Test deltaX returns horizontal component
  - Test deltaY returns vertical component
  - Test negative deltas for leftward/upward swipes
  - **Expected**: Tests FAIL (Red)

- [X] T003 Implement SwipeGesture data class in domain/model/SwipeGesture.kt
  - Fields: startPosition: Offset, endPosition: Offset
  - Computed property: delta: Offset
  - Method: getDistance(): Float
  - Computed property: deltaX: Float
  - Computed property: deltaY: Float
  - **Expected**: Tests PASS (Green)

---

## Phase 3: User Story 1 (P1) - Directional Control via Swipe

**Priority**: P1 (Critical - MVP)
**Goal**: Players control snake direction by swiping on screen (up/down/left/right)

**Acceptance Criteria** (from spec.md):
1. Swipe upward → snake changes direction to move upward
2. Swipe left → snake changes direction to move left
3. Swipe right → snake changes direction to move right
4. Swipe down → snake changes direction to move down

**Independent Test**: Display moving snake on screen, verify swipe gestures in each cardinal direction change snake movement direction.

### Tasks

#### 3.1 DetectSwipeDirectionUseCase (TDD) - 35 min

- [X] T004 [P] [US1] Write tests for DetectSwipeDirectionUseCase in domain/usecase/DetectSwipeDirectionUseCaseTest.kt
  - Test swipe right (above threshold) returns Direction.RIGHT
  - Test swipe left (above threshold) returns Direction.LEFT
  - Test swipe up (above threshold) returns Direction.UP
  - Test swipe down (above threshold) returns Direction.DOWN
  - Test swipe below minimum distance returns null
  - Test diagonal swipe with horizontal dominance returns horizontal direction
  - Test diagonal swipe with vertical dominance returns vertical direction
  - Test exactly equal deltaX and deltaY defaults to vertical
  - Test zero distance returns null
  - **Expected**: Tests FAIL (Red)

- [X] T005 [US1] Implement DetectSwipeDirectionUseCase in domain/usecase/DetectSwipeDirectionUseCase.kt
  - Constructor: inject Density for Dp-to-px conversion
  - Operator fun invoke(gesture: SwipeGesture): Direction?
  - Check gesture.getDistance() >= MIN_SWIPE_DISTANCE (converted to px)
  - If below threshold: return null
  - Compare abs(gesture.deltaX) vs abs(gesture.deltaY)
  - Determine dominant axis and direction sign
  - Return appropriate Direction enum value
  - **Expected**: Tests PASS (Green)

#### 3.2 SwipeGestureDetector Modifier (TDD) - 60 min

- [X] T006 [US1] Write Compose UI tests for SwipeGestureDetector in ui/game/SwipeGestureDetectorTest.kt
  - Test swipe right above threshold invokes callback with RIGHT
  - Test swipe left above threshold invokes callback with LEFT
  - Test swipe up above threshold invokes callback with UP
  - Test swipe down above threshold invokes callback with DOWN
  - Test short swipe below threshold does NOT invoke callback
  - Test diagonal swipe resolves to dominant axis direction
  - Test rapid consecutive swipes invoke callback for each
  - **Expected**: Tests FAIL (Red)

- [X] T007 [US1] Implement SwipeGestureDetector composable modifier in ui/game/SwipeGestureDetector.kt
  - @Composable fun Modifier.swipeGestureDetector(minSwipeDistance: Dp, onSwipe: (Direction) -> Unit): Modifier
  - Use LocalDensity.current for density
  - remember { DetectSwipeDirectionUseCase(density) }
  - Track accumulated drag with remember { mutableStateOf(Offset.Zero) }
  - Use Modifier.pointerInput(Unit) with detectDragGestures
  - onDragStart: reset accumulatedDrag
  - onDrag: consume event, accumulate dragAmount
  - onDragEnd: create SwipeGesture, call DetectSwipeDirectionUseCase, invoke onSwipe if non-null
  - **Expected**: Tests PASS (Green)

#### 3.3 GameScreen Integration (TDD) - 30 min

- [X] T008 [US1] Write integration test for GameScreen in ui/game/GameScreenTest.kt
  - Test swipe gesture updates ViewModel direction
  - Test swipe up changes gameState.snake.direction to UP
  - Test swipe right changes gameState.snake.direction to RIGHT
  - **Expected**: Tests FAIL (Red)

- [X] T009 [US1] Create GameScreen composable in ui/game/GameScreen.kt
  - @Composable fun GameScreen(viewModel: GameViewModel, modifier: Modifier)
  - Collect gameState from viewModel.gameState.collectAsState()
  - Box with fillMaxSize and swipeGestureDetector modifier
  - swipeGestureDetector callback: viewModel.handleDirectionInput(direction)
  - TODO: Render snake, food, score (placeholder for future features)
  - **Expected**: Tests PASS (Green)

#### 3.4 User Story 1 Validation

- [ ] T010 [US1] Run all US1 tests and verify passing
  - SwipeGestureTest (all passing)
  - DetectSwipeDirectionUseCaseTest (all passing)
  - SwipeGestureDetectorTest (all passing)
  - GameScreenTest (all passing)
  - **Expected**: 100% test pass rate for US1

- [ ] T011 [US1] Manual testing of US1 acceptance criteria
  - Build and install on device/emulator
  - Test swipe up → snake moves up
  - Test swipe down → snake moves down
  - Test swipe left → snake moves left
  - Test swipe right → snake moves right
  - **Expected**: All 4 acceptance criteria pass

---

## Phase 4: User Story 2 (P2) - Prevent Reverse Direction

**Priority**: P2 (High - prevents frustrating instant death)
**Goal**: Players cannot reverse snake direction 180 degrees with a single swipe

**Acceptance Criteria** (from spec.md):
1. Snake moving right, swipe left → snake continues right (ignores reverse)
2. Snake moving up, swipe down → snake continues up
3. Snake moving down, swipe up → snake continues down
4. Snake moving left, swipe right → snake continues left

**Independent Test**: Attempt to swipe in opposite direction of current movement, verify snake continues in original direction.

**Dependencies**: US1 must be complete (requires swipe detection and direction changes working)

### Tasks

#### 4.1 Reverse Direction Prevention (Validation) - 10 min

**Note**: ValidateDirectionUseCase already exists and is integrated into GameViewModel.handleDirectionInput(). This user story verifies the integration works correctly with swipe controls.

- [ ] T012 [P] [US2] Expand GameViewModelTest in ui/game/GameViewModelTest.kt
  - Test swipe LEFT when moving RIGHT is ignored (direction remains RIGHT)
  - Test swipe DOWN when moving UP is ignored (direction remains UP)
  - Test swipe UP when moving DOWN is ignored (direction remains DOWN)
  - Test swipe RIGHT when moving LEFT is ignored (direction remains LEFT)
  - **Expected**: Tests pass (existing ValidateDirectionUseCase handles this)

#### 4.2 User Story 2 Validation

- [ ] T013 [US2] Manual testing of US2 acceptance criteria
  - Snake moving right, swipe left → verify direction remains right
  - Snake moving up, swipe down → verify direction remains up
  - Snake moving down, swipe up → verify direction remains down
  - Snake moving left, swipe right → verify direction remains left
  - **Expected**: All 4 acceptance criteria pass

---

## Phase 5: User Story 3 (P3) - Responsive Swipe Detection

**Priority**: P3 (Nice-to-have - polish and responsiveness)
**Goal**: Swipe gestures detected quickly and reliably with immediate visual feedback

**Acceptance Criteria** (from spec.md):
1. Direction change occurs within 100 milliseconds of swipe completion
2. Multiple valid swipes in quick succession are queued and executed in order
3. Very short swipes meeting minimum distance threshold are registered
4. Ambiguous diagonal swipes are resolved by dominant axis

**Independent Test**: Perform rapid direction changes and measure delay between swipe and direction change. Test edge cases like diagonal swipes and short swipes.

**Dependencies**: US1 must be complete (requires swipe detection working)

### Tasks

#### 5.1 Performance & Edge Case Testing - 30 min

- [ ] T014 [P] [US3] Write performance tests for swipe latency
  - Create performance test measuring time from onDragEnd to direction update
  - Verify latency < 100ms (acceptance criterion 1)
  - **Note**: May use JUnit @Test with timing assertions or Macrobenchmark

- [ ] T015 [P] [US3] Test rapid consecutive swipes in SwipeGestureDetectorTest
  - Perform swipe right → swipe up → swipe left in rapid succession
  - Verify each direction change is processed
  - **Expected**: Already passing from T006, validate timing

- [ ] T016 [P] [US3] Test minimum distance threshold edge cases
  - Test swipe at exactly MIN_SWIPE_DISTANCE (should trigger)
  - Test swipe at MIN_SWIPE_DISTANCE - 1px (should not trigger)
  - Test perfectly diagonal swipe (45 degrees, equal X and Y)
  - **Expected**: Already covered in T004-T006, validate edge values

#### 5.2 Optional: Input Queue (Deferred)

**Decision**: Start without input queue (MAX_QUEUED_DIRECTIONS = 0). Add only if user testing shows missed swipes.

- [ ] T017 [US3] (OPTIONAL) Implement input queue if needed
  - Extend GameState with pendingDirectionChanges: List<Direction>
  - Modify GameViewModel to queue direction changes
  - Process queue in game loop
  - Add tests for queue behavior
  - **Condition**: Only if manual testing reveals missed rapid swipes

#### 5.3 User Story 3 Validation

- [ ] T018 [US3] Manual testing of US3 acceptance criteria
  - Perform swipe and measure perceived latency (should feel <100ms)
  - Perform rapid swipes (up-left-down-right quickly)
  - Test very short swipes near threshold
  - Test diagonal swipes (45 degrees, 30 degrees, etc.)
  - **Expected**: All acceptance criteria met

- [ ] T019 [US3] Performance validation with Android Studio Profiler
  - Enable GPU Rendering profiler
  - Perform rapid swipes and verify 60 FPS maintained
  - Check frame time bars stay below 16ms line
  - Verify no dropped frames during swipe gestures
  - **Expected**: Consistent 60 FPS performance

---

## Phase 6: Polish & Cross-Cutting Concerns

**Goal**: Final integration, cleanup, and documentation

### Tasks

#### 6.1 Integration & Regression Testing

- [ ] T020 [P] Run full test suite
  - Execute: `./gradlew test`
  - Verify all unit tests pass
  - Verify all existing tests (Direction, ValidateDirectionUseCase, GameViewModel) still pass
  - **Expected**: 100% pass rate, no regressions

- [ ] T021 [P] Run Android instrumentation tests
  - Execute: `./gradlew connectedAndroidTest`
  - Verify all Compose UI tests pass
  - **Expected**: All instrumentation tests pass on device/emulator

#### 6.2 Code Quality

- [ ] T022 [P] Run Kotlin linter
  - Execute: `./gradlew lintDebug` (if configured)
  - Fix any lint warnings or errors
  - **Expected**: No lint issues

- [ ] T023 Verify Constitution compliance
  - Review implementation against Constitution Principle III (TDD)
  - Confirm all tests written before implementation (Red-Green-Refactor)
  - Verify no constitution violations in Complexity Tracking
  - **Expected**: Full compliance

#### 6.3 Documentation

- [ ] T024 [P] Update implementation notes in plan.md
  - Document any deviations from plan
  - Note any deferred features (e.g., input queue)
  - Record actual implementation time vs. estimates
  - **Expected**: plan.md reflects actual implementation

- [ ] T025 Final manual test of all user stories
  - Run through all acceptance criteria for US1, US2, US3
  - Test on different screen sizes (phone, tablet if available)
  - Test in portrait and landscape orientations
  - Record any issues or improvements for future features
  - **Expected**: All user stories deliver specified value

---

## Dependency Graph

### User Story Completion Order

```
Phase 1: Setup
    ↓
Phase 2: Foundational Models (blocks all user stories)
    ↓
Phase 3: User Story 1 (P1) ← MVP DELIVERY POINT
    ↓
Phase 4: User Story 2 (P2) ← depends on US1
    ↓
Phase 5: User Story 3 (P3) ← depends on US1
    ↓
Phase 6: Polish
```

### Task Dependencies Within User Stories

**User Story 1** (can work independently after Phase 2):
```
T004 (Tests) ─→ T005 (Use Case Implementation)
T006 (Tests) ─→ T007 (UI Modifier Implementation)
T008 (Tests) ─→ T009 (GameScreen Implementation)
    ↓
T010 (Validation)
    ↓
T011 (Manual Testing)
```

**User Story 2** (requires US1 complete):
```
US1 Complete ─→ T012 (Tests) ─→ T013 (Manual Testing)
```

**User Story 3** (requires US1 complete):
```
US1 Complete ─→ T014, T015, T016 (Tests) ─→ T017 (Optional) ─→ T018, T019 (Validation)
```

---

## Parallel Execution Opportunities

### Phase 2: Foundational Models
- **T002-T003**: SwipeGesture model (single developer, sequential TDD)

### Phase 3: User Story 1
**After T002-T003 complete**:
- T004 (DetectSwipeDirectionUseCase tests) can run in parallel with:
  - T006 (SwipeGestureDetector tests)
  - T008 (GameScreen tests)
- **Parallelization**: 3 developers can write tests simultaneously

**After tests written**:
- T005, T007, T009 must be sequential (depend on their respective tests)

### Phase 4: User Story 2
- T012: Single task, no parallelization within phase

### Phase 5: User Story 3
- T014, T015, T016: All parallelizable (different test aspects)
- **Parallelization**: 3 developers can run simultaneously

### Phase 6: Polish
- T020, T021, T022, T024: All parallelizable
- T023, T025: Sequential (depend on integration)
- **Parallelization**: Up to 4 developers for testing/linting/docs

---

## Testing Summary

### Test Coverage by User Story

**User Story 1** (P1):
- SwipeGestureTest: 6 test cases
- DetectSwipeDirectionUseCaseTest: 9 test cases
- SwipeGestureDetectorTest: 7 test cases
- GameScreenTest: 2 integration test cases
- **Total**: 24 automated tests + 4 manual acceptance criteria

**User Story 2** (P2):
- GameViewModelTest (expanded): 4 test cases
- **Total**: 4 automated tests + 4 manual acceptance criteria

**User Story 3** (P3):
- Performance tests: 1 test case
- Edge case tests: 2 test cases (validation of existing coverage)
- **Total**: 3 automated tests + 4 manual acceptance criteria + performance profiling

**Grand Total**: 31 automated tests + 12 manual acceptance criteria

---

## Implementation Checklist

### Pre-Implementation
- [x] Constitution Check passed (plan.md)
- [x] All design documents generated (plan, research, data-model, contracts, quickstart)
- [x] User stories prioritized (P1, P2, P3)
- [ ] Development environment ready (Android Studio, Kotlin 1.9.20, Compose)

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
- [ ] Performance validated (60 FPS, <100ms latency)
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
- ✅ All 31 automated tests passing
- ✅ All 12 manual acceptance criteria met
- ✅ 60 FPS performance maintained
- ✅ <100ms swipe latency achieved
- ✅ No reverse direction inputs processed
- ✅ TDD workflow followed throughout

---

**Ready to implement!** Start with Phase 1, Task T001.
