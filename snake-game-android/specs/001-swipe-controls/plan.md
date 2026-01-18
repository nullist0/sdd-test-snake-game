# Implementation Plan: Swipe-Based Snake Controls

**Branch**: `001-swipe-controls` | **Date**: 2026-01-18 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-swipe-controls/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement swipe-based directional controls for the snake game using Android Compose gesture detection. The primary requirement is to allow players to control snake direction through swipe gestures (up, down, left, right) while preventing reverse-direction inputs that would cause instant self-collision. The technical approach will use Compose's gesture detection APIs (detectDragGestures or pointerInput) to capture swipe gestures, determine direction from start/end positions, and integrate with the existing GameViewModel for direction validation.

## Technical Context

**Language/Version**: Kotlin 1.9.20 for Android
**Primary Dependencies**:
- Jetpack Compose (BOM 2023.10.01) for UI and gesture detection
- AndroidX Lifecycle & ViewModel (2.7.0) for state management
- Kotlin Coroutines (1.7.3) for asynchronous processing
- Material3 for UI components

**Storage**: N/A (game state held in memory via ViewModel StateFlow)
**Testing**:
- JUnit 4.13.2 for unit tests
- Kotlin Test 1.9.20 for Kotlin-specific test utilities
- Coroutines Test 1.7.3 for flow testing
- Compose UI Test for gesture integration testing

**Target Platform**: Android SDK 24+ (Android 7.0 Nougat), compileSdk 34
**Project Type**: Mobile (Android single-module application)
**Performance Goals**:
- 60 FPS during gameplay and gesture processing
- <100ms latency from swipe gesture completion to direction change
- Smooth gesture detection with no dropped touches

**Constraints**:
- Offline-only (no network required)
- Minimal memory footprint (game state only)
- Support portrait and landscape orientations
- Work across diverse screen sizes (phones and tablets)

**Scale/Scope**:
- Single-player local game
- ~10-15 source files for complete implementation
- Compose-based UI with clean architecture (domain, UI layers)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Feature-First Development ✅
- Feature spec defines user scenarios with testable acceptance criteria (User Stories 1-3)
- Functional requirements are measurable and unambiguous (FR-001 through FR-010)
- Success criteria defined from user perspective (SC-001 through SC-007)
- No implementation details in specification (Compose/gestures mentioned only in plan)
**Status**: PASS

### Principle II: User Experience Priority ✅
- Touch controls optimized for mobile gameplay (swipe-based directional control)
- Responsive UI target: <100ms latency from swipe to direction change (FR-002)
- 60 FPS performance goal maintained during gesture processing (FR-008, SC-003)
- Clear visual feedback: existing snake direction visualization (assumption documented)
- Screen size compatibility: gesture detection works across different screen sizes (FR-009)
**Status**: PASS

### Principle III: Test-Before-Implementation (NON-NEGOTIABLE) ✅
- TDD workflow will be enforced during implementation phase
- Tests must be written before gesture detection implementation
- Tests must fail initially (proving they test the right behavior)
- Unit tests for SwipeGestureDetector component
- Integration tests for gesture → ViewModel → state flow
- Contract tests between UI gesture layer and domain validation
**Status**: PASS (to be enforced in implementation phase via /speckit.tasks and /speckit.implement)

### Principle IV: Performance & Efficiency ✅
- 60 FPS target explicitly defined (FR-008, SC-003)
- <100ms gesture latency requirement (FR-002, SC-002)
- Memory: minimal footprint, state held in ViewModel only (Technical Context)
- Battery: no continuous polling, event-driven gesture detection
- Async processing: Coroutines for non-blocking state updates
**Status**: PASS

### Principle V: Code Simplicity & Maintainability ✅
- YAGNI: implementing only required swipe detection, no premature abstractions
- Clean architecture: domain (Direction, ValidateDirectionUseCase) separate from UI
- Modular: SwipeGestureDetector as reusable component
- Clear naming: Direction, SwipeGesture, GameViewModel
- No over-engineering: single-finger swipe only (multi-touch out of scope)
**Status**: PASS

### Mobile Development Constraints ✅
- **Minimum SDK**: Android API 24+ (defined in Technical Context, matches build.gradle.kts)
- **Permissions**: None required (touch input standard)
- **Lifecycle Management**: ViewModel survives configuration changes
- **Orientation**: Portrait/landscape support (constraint documented)
- **Offline Support**: Fully offline, no network dependencies
- **Security**: No sensitive data, no external APIs
- **Quality Standards**: No ANRs (gesture detection non-blocking), memory leaks prevented by ViewModel lifecycle
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
specs/[###-feature]/
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
│   │   │   │   │   └── SwipeGesture.kt (NEW - for this feature)
│   │   │   │   └── usecase/
│   │   │   │       ├── ValidateDirectionUseCase.kt (existing)
│   │   │   │       └── DetectSwipeDirectionUseCase.kt (NEW - for this feature)
│   │   │   └── ui/
│   │   │       ├── game/
│   │   │       │   ├── GameViewModel.kt (existing, will be modified)
│   │   │       │   ├── GameState.kt (existing)
│   │   │       │   ├── GameScreen.kt (NEW - main Compose screen)
│   │   │       │   └── SwipeGestureDetector.kt (NEW - gesture modifier)
│   │   │       └── theme/ (existing)
│   │   └── AndroidManifest.xml (existing)
│   └── test/
│       └── java/com/snakegame/
│           ├── domain/
│           │   ├── model/
│           │   │   ├── DirectionTest.kt (existing)
│           │   │   └── SwipeGestureTest.kt (NEW)
│           │   └── usecase/
│           │       ├── ValidateDirectionUseCaseTest.kt (existing)
│           │       └── DetectSwipeDirectionUseCaseTest.kt (NEW)
│           └── ui/
│               └── game/
│                   ├── GameViewModelTest.kt (existing, will be expanded)
│                   └── SwipeGestureDetectorTest.kt (NEW)
└── build.gradle.kts (existing, Compose already enabled)
```

**Structure Decision**:
This is an Android single-module mobile application following clean architecture principles. The project separates concerns into:
- **domain/model**: Core data models (Direction, Snake, SwipeGesture)
- **domain/usecase**: Business logic (direction validation, swipe detection)
- **ui/game**: Compose UI layer (ViewModel, Composables, gesture detection)

Existing infrastructure supports this feature:
- Direction model and validation logic already implemented
- GameViewModel exists and handles direction changes
- Compose dependencies already configured in build.gradle.kts

New components for swipe control:
- SwipeGesture model to represent touch gesture data
- DetectSwipeDirectionUseCase to convert gestures to directions
- SwipeGestureDetector composable modifier to capture swipe events
- GameScreen composable to render the game UI with gesture handling

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
