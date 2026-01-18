# Research: No Reverse Direction Control

**Feature**: 009-no-reverse-direction
**Date**: 2026-01-18
**Phase**: 0 - Research & Technical Decisions

## Overview

This document captures research findings and technical decisions for implementing reverse direction prevention in the snake game. The research focuses on direction validation algorithms, Android game loop integration, and performance considerations for real-time input processing.

## Research Questions

### 1. Direction Validation Algorithm

**Question**: What is the most efficient algorithm for detecting reverse direction in a 4-directional grid-based game?

**Decision**: Enum-based direction pairs with constant-time lookup

**Rationale**:
- **Approach**: Use Kotlin enum with companion object mapping each direction to its reverse
- **Implementation**: `Direction.UP.reverse() == Direction.DOWN` using sealed enum or companion object
- **Performance**: O(1) lookup, no computation required
- **Memory**: Minimal (4 enum constants + 4 reverse mappings = ~32 bytes)
- **Type Safety**: Kotlin sealed enum prevents invalid states at compile time

**Alternatives Considered**:
1. **Angle-based calculation** (newAngle = (oldAngle + 180) % 360):
   - Rejected: Unnecessary computation overhead for only 4 directions
   - Rejected: Floating-point arithmetic slower than enum comparison
   - Rejected: Requires angle normalization and modulo operations

2. **Vector dot product** (dot(currentDir, newDir) == -1 means reverse):
   - Rejected: Overkill for cardinal directions (works for any angle but unnecessary)
   - Rejected: Vector representation adds memory overhead
   - Rejected: Dot product calculation slower than enum lookup

3. **Switch/when expression** (manual mapping in when block):
   - Rejected: Less maintainable than enum companion object
   - Rejected: No compile-time guarantees of exhaustiveness
   - Considered: Could be used as fallback implementation

**Code Pattern**:
```kotlin
enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun reverse(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    fun isReverse(other: Direction): Boolean = this.reverse() == other

    fun isPerpendicular(other: Direction): Boolean =
        this != other && !isReverse(other)
}
```

### 2. Input Validation Timing

**Question**: Should direction validation happen before or after swipe gesture processing, and how does this integrate with the game loop?

**Decision**: Validate immediately after swipe-to-direction conversion, before applying to game state

**Rationale**:
- **Flow**: SwipeGesture → Direction → Validate → Apply (if valid)
- **Early rejection**: Invalid directions filtered before reaching game state
- **Separation of concerns**: Swipe detector handles gesture recognition, validator handles game rules
- **Testability**: Validation logic isolated in pure function/use case
- **Performance**: Validation happens on UI thread but is O(1) operation (<1ms)

**Alternatives Considered**:
1. **Validate during snake movement calculation**:
   - Rejected: Too late in the pipeline, game state already modified
   - Rejected: Harder to test in isolation
   - Rejected: Mixes movement logic with input validation

2. **Validate in swipe detector**:
   - Rejected: Swipe detector should be stateless gesture recognizer
   - Rejected: Requires injecting current direction state into gesture detector
   - Rejected: Violates single responsibility principle

3. **No validation, rely on collision detection**:
   - Rejected: Allows instant self-collision, violates spec requirement
   - Rejected: Poor UX (player feels controls are broken)
   - Rejected: Defeats purpose of reverse prevention feature

**Integration Pattern**:
```kotlin
// UI Layer (GameScreen.kt)
SwipeDetector.onSwipe { swipeDirection ->
    val requestedDirection = swipeDirection.toGameDirection()
    viewModel.handleDirectionInput(requestedDirection)
}

// ViewModel (GameViewModel.kt)
fun handleDirectionInput(requested: Direction) {
    val isValid = validateDirectionUseCase(
        current = gameState.snake.direction,
        requested = requested
    )
    if (isValid) {
        updateDirection(requested)
    }
    // Invalid directions silently ignored (spec requirement)
}
```

### 3. State Management for Current Direction

**Question**: Where should the current direction state be stored, and how should it be accessed for validation?

**Decision**: Store in ViewModel as part of Snake entity state, pass explicitly to validation use case

**Rationale**:
- **Single source of truth**: Direction is part of Snake state, managed by ViewModel
- **Lifecycle awareness**: ViewModel survives configuration changes (screen rotation)
- **Testability**: Validation use case receives direction as parameter (pure function)
- **Android best practice**: Game state in ViewModel, UI observes state as Flow/LiveData
- **No global state**: Explicit dependency injection for validation use case

**Alternatives Considered**:
1. **Global singleton game state**:
   - Rejected: Violates Clean Architecture principles
   - Rejected: Harder to test (global mutable state)
   - Rejected: Not lifecycle-aware (memory leaks possible)

2. **Store in validation use case itself**:
   - Rejected: Use case should be stateless
   - Rejected: Requires singleton use case instance
   - Rejected: Violates single responsibility (validation, not state management)

3. **Store in Repository layer**:
   - Considered: Valid option for multi-screen games
   - Rejected for this project: Single-screen game, ViewModel sufficient
   - Rejected: Over-engineering for no persistent storage requirement

**State Flow Pattern**:
```kotlin
// GameViewModel.kt
data class GameState(
    val snake: Snake,
    val fruit: Fruit,
    val score: Int,
    val isGameOver: Boolean
)

data class Snake(
    val head: Position,
    val body: List<Position>,
    val direction: Direction  // Current direction stored here
)

private val _gameState = MutableStateFlow(GameState.initial())
val gameState: StateFlow<GameState> = _gameState.asStateFlow()
```

### 4. Android Game Loop Architecture

**Question**: How should direction validation integrate with Android's UI thread and game loop timing?

**Decision**: Use Kotlin Coroutines with fixed-interval game loop, validate synchronously on input event

**Rationale**:
- **Game loop**: Coroutine with `delay(16ms)` for ~60 FPS (or `withFrameNanos` for precise timing)
- **Input handling**: Synchronous validation on UI thread (fast enough for <1ms enum comparison)
- **Thread safety**: Single-threaded game loop (no concurrent direction changes)
- **Android compatibility**: Coroutines are Android best practice (works with Jetpack Compose)
- **Performance**: Validation doesn't block game loop (happens on separate input events)

**Alternatives Considered**:
1. **Handler.postDelayed game loop**:
   - Rejected: Coroutines are more idiomatic Kotlin
   - Rejected: Handler requires manual cancellation management
   - Considered: Valid alternative for developers familiar with Handler

2. **RxJava interval observable**:
   - Rejected: Adds heavy dependency for simple game loop
   - Rejected: Coroutines are lighter weight and official Android recommendation
   - Rejected: Steeper learning curve for new Android developers

3. **Native game loop (NDK/C++)**:
   - Rejected: Massive overkill for turn-based snake game
   - Rejected: No performance benefit (validation is already <1ms)
   - Rejected: Complicates development and testing

**Game Loop Pattern**:
```kotlin
// GameViewModel.kt
private var gameLoopJob: Job? = null

fun startGame() {
    gameLoopJob = viewModelScope.launch {
        while (isActive) {
            moveSnake()  // Uses validated direction from state
            checkCollision()
            checkVictory()
            _gameState.update { /* new state */ }
            delay(200)  // 200ms = ~5 moves/second (adjust for difficulty)
        }
    }
}

fun stopGame() {
    gameLoopJob?.cancel()
}
```

### 5. Testing Strategy for Direction Validation

**Question**: What testing approach ensures all direction pairs and edge cases are covered?

**Decision**: Parameterized unit tests for all direction combinations + integration tests for swipe-to-validation flow

**Rationale**:
- **Unit tests**: JUnit parameterized tests cover all 16 direction pair combinations (4 current × 4 requested)
- **Expected results**: 4 reverse (rejected), 4 same (accepted), 8 perpendicular (accepted)
- **Edge cases**: Rapid input sequences, null/initial direction, timing boundary conditions
- **Integration tests**: Espresso UI tests for swipe gesture → direction validation flow
- **TDD approach**: Write tests first, then implement Direction enum and validation logic

**Test Coverage Plan**:
```kotlin
// ValidateDirectionUseCaseTest.kt (Unit tests - write FIRST per TDD)
@RunWith(Parameterized::class)
class ValidateDirectionUseCaseTest(
    private val current: Direction,
    private val requested: Direction,
    private val expected: Boolean
) {

    companion object {
        @JvmStatic
        @Parameters(name = "current={0}, requested={1}, valid={2}")
        fun data() = listOf(
            // Reverse direction cases (should reject)
            arrayOf(Direction.UP, Direction.DOWN, false),
            arrayOf(Direction.DOWN, Direction.UP, false),
            arrayOf(Direction.LEFT, Direction.RIGHT, false),
            arrayOf(Direction.RIGHT, Direction.LEFT, false),

            // Same direction cases (should accept)
            arrayOf(Direction.UP, Direction.UP, true),
            arrayOf(Direction.DOWN, Direction.DOWN, true),
            arrayOf(Direction.LEFT, Direction.LEFT, true),
            arrayOf(Direction.RIGHT, Direction.RIGHT, true),

            // Perpendicular cases (should accept)
            arrayOf(Direction.UP, Direction.LEFT, true),
            arrayOf(Direction.UP, Direction.RIGHT, true),
            arrayOf(Direction.DOWN, Direction.LEFT, true),
            arrayOf(Direction.DOWN, Direction.RIGHT, true),
            arrayOf(Direction.LEFT, Direction.UP, true),
            arrayOf(Direction.LEFT, Direction.DOWN, true),
            arrayOf(Direction.RIGHT, Direction.UP, true),
            arrayOf(Direction.RIGHT, Direction.DOWN, true),
        )
    }

    @Test
    fun `validate direction returns expected result`() {
        val useCase = ValidateDirectionUseCase()
        val result = useCase(current, requested)
        assertEquals(expected, result)
    }
}
```

**Alternatives Considered**:
1. **Manual test cases for each combination**:
   - Rejected: 16 duplicate test methods (harder to maintain)
   - Rejected: Parameterized tests provide better coverage visibility

2. **Property-based testing (QuickCheck-style)**:
   - Considered: Interesting for fuzzing edge cases
   - Rejected: Overkill for exhaustive 16-case domain
   - Possible future enhancement: Random rapid input sequences

3. **Integration tests only**:
   - Rejected: Slower execution (requires Android framework)
   - Rejected: Harder to isolate validation logic failures
   - Insufficient: Need both unit and integration tests per constitution

## Technology Stack Decisions

### Primary Technologies

| Technology | Version | Justification |
|------------|---------|---------------|
| Kotlin | 1.9+ | Official Android language, null safety, sealed classes for Direction enum |
| Android SDK | API 24+ (Nougat) | User-specified minimum, covers 94%+ devices (2026 data) |
| Jetpack Compose | 1.5+ | Modern declarative UI, easier game rendering than XML layouts |
| Kotlin Coroutines | 1.7+ | Game loop implementation, official concurrency recommendation |
| JUnit 4/5 | 4.13+/5.9+ | Standard unit testing, parameterized test support |
| Espresso | 3.5+ | Android UI testing for swipe integration tests |

### Build Configuration

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| Build System | Gradle 8.2+ with Kotlin DSL | Standard Android build, type-safe configuration |
| Minimum SDK | 24 (Android 7.0) | User requirement, broad device compatibility |
| Target SDK | 34 (Android 14) | Latest stable, required for Play Store (2026) |
| Compile SDK | 34 | Must be >= Target SDK |
| JVM Target | 1.8 | Required for Android API 24+, Kotlin coroutines compatibility |

## Performance Benchmarks

### Direction Validation Performance

**Target**: <1ms validation time to avoid impacting 60 FPS game loop (16.67ms frame budget)

**Expected Performance**:
- Enum comparison: ~0.0001ms (100 nanoseconds) on modern Android devices
- Use case invocation overhead: ~0.001ms (1 microsecond)
- Total validation latency: <0.01ms (10 microseconds), well under 1ms budget

**Measurement Strategy**:
- Microbenchmark using `measureNanoTime { }` for 10,000 iterations
- Average, p95, p99 latencies recorded
- Test on minimum spec device (Android 7.0 on low-end 2017 hardware)

### Memory Footprint

**Target**: Minimal overhead (<1KB for direction validation logic)

**Expected Footprint**:
- Direction enum: 4 constants × 8 bytes = 32 bytes
- Validation use case: Single instance, ~100 bytes object overhead
- State in ViewModel: 1 Direction reference = 8 bytes (pointer)
- Total: <1KB, negligible impact on 50MB app budget

## Best Practices Applied

### Kotlin Best Practices

1. **Sealed Enum for Direction**: Compile-time exhaustiveness checking
2. **Extension Functions**: `Direction.isReverse(other)` for readable validation
3. **When Expression**: Exhaustive pattern matching for direction mappings
4. **Null Safety**: No nullable directions (always has a current direction)
5. **Data Classes**: Immutable Snake and Position for thread safety

### Android Best Practices

1. **ViewModel for State**: Lifecycle-aware, survives configuration changes
2. **StateFlow for UI State**: Modern reactive state management (vs LiveData)
3. **Coroutines for Game Loop**: Structured concurrency, proper cancellation
4. **Clean Architecture**: UI → Domain → Data separation for testability
5. **Material Design**: Follow Android design guidelines for UI elements

### Testing Best Practices (TDD)

1. **Red-Green-Refactor**: Write failing tests first, implement to pass, refactor
2. **Parameterized Tests**: Cover all 16 direction combinations systematically
3. **Given-When-Then**: BDD-style test structure for clarity
4. **Fast Unit Tests**: Pure Kotlin tests run in <1 second total
5. **Integration Tests**: Verify swipe gesture → validation → state update flow

## Risk Mitigation

### Risk 1: Input Race Conditions

**Risk**: Rapid swipes could queue multiple direction changes, potentially allowing reverse through timing

**Mitigation**:
- **Solution**: Single-threaded game state (ViewModel on UI thread)
- **Approach**: Only most recent direction input is used, no input queue
- **Validation**: Integration tests simulate rapid swipe sequences
- **Fallback**: Direction change rate limiting (max 1 change per game tick)

### Risk 2: Configuration Changes

**Risk**: Screen rotation could reset direction state, causing unexpected behavior

**Mitigation**:
- **Solution**: ViewModel survives configuration changes automatically
- **Approach**: Direction stored in ViewModel, not Activity/Fragment
- **Validation**: Rotation tests verify direction persists
- **Documentation**: Clearly document state management in quickstart.md

### Risk 3: Performance on Low-End Devices

**Risk**: Validation might impact frame rate on slow 2017-era devices (Android 7.0)

**Mitigation**:
- **Solution**: O(1) enum validation is fast enough for any device
- **Approach**: Profile on minimum spec device (Android 7.0, 1GB RAM, quad-core 1.4GHz)
- **Validation**: Frame rate monitoring during validation stress test
- **Fallback**: None needed (validation is already optimal)

## Open Questions

None remaining - all technical decisions finalized.

## Next Steps

Phase 1 outputs:
1. **data-model.md**: Document Direction enum and Snake state model
2. **contracts/DirectionValidator.kt.md**: Define validation use case interface
3. **quickstart.md**: Developer guide for implementing direction validation with TDD

Phase 2 (separate command):
4. **tasks.md**: Break down implementation into TDD-ordered tasks (tests first, then impl)
