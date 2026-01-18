# Contract: ValidateDirectionUseCase

**Feature**: 009-no-reverse-direction
**Type**: Domain Use Case (Business Logic)
**Layer**: Domain
**Date**: 2026-01-18

## Purpose

Validates whether a requested direction change is allowed based on the current snake movement direction. Implements the reverse direction prevention rule: blocks 180-degree turns that would cause immediate self-collision.

## Interface Contract

### Kotlin Interface

```kotlin
package com.snakegame.domain.usecase

/**
 * Validates direction changes according to snake game rules.
 *
 * Rule: Snake cannot reverse direction (180-degree turn).
 * - Reverse directions are rejected (UP ↔ DOWN, LEFT ↔ RIGHT)
 * - Perpendicular directions are accepted (90-degree turns)
 * - Same direction is accepted (continue current path)
 *
 * This prevents instant self-collision from reversing into snake body.
 */
interface ValidateDirectionUseCase {
    /**
     * Validates a requested direction change.
     *
     * @param current The snake's current movement direction
     * @param requested The direction requested by player input
     * @return true if direction change is allowed, false if rejected
     *
     * @throws IllegalStateException never - all direction combinations are valid inputs
     */
    operator fun invoke(current: Direction, requested: Direction): Boolean
}
```

### Alternative: Detailed Result

```kotlin
/**
 * Alternative contract with detailed validation result.
 * Use if you need to distinguish rejection reasons or log validation outcomes.
 */
interface ValidateDirectionUseCase {
    operator fun invoke(
        current: Direction,
        requested: Direction
    ): DirectionValidationResult
}

sealed class DirectionValidationResult {
    data class Valid(val newDirection: Direction) : DirectionValidationResult()
    data class Rejected(val reason: RejectionReason) : DirectionValidationResult()
}

enum class RejectionReason {
    REVERSE_NOT_ALLOWED
}
```

**Recommendation**: Use simple Boolean contract for MVP (aligns with YAGNI principle). Detailed result adds complexity without clear user benefit.

## Input Parameters

### `current: Direction`

**Description**: The direction the snake is currently moving

**Validation**:
- Must be non-null (Direction is enum, cannot be null)
- Must be one of: `Direction.UP`, `Direction.DOWN`, `Direction.LEFT`, `Direction.RIGHT`
- Represents the last validated direction applied to snake state

**Source**: Typically from `gameState.snake.direction` in ViewModel

**Example Values**:
```kotlin
Direction.UP      // Snake moving upward
Direction.DOWN    // Snake moving downward
Direction.LEFT    // Snake moving leftward
Direction.RIGHT   // Snake moving rightward
```

---

### `requested: Direction`

**Description**: The direction the player wants to change to

**Validation**:
- Must be non-null (Direction is enum, cannot be null)
- Must be one of: `Direction.UP`, `Direction.DOWN`, `Direction.LEFT`, `Direction.RIGHT`
- Represents the direction derived from player's swipe gesture

**Source**: Typically from `SwipeGestureDetector.toDirection()` in UI layer

**Example Values**:
```kotlin
Direction.LEFT   // Player swiped left
Direction.RIGHT  // Player swiped right
Direction.UP     // Player swiped up
Direction.DOWN   // Player swiped down
```

## Output

### Boolean Result (Simple Contract)

**Type**: `Boolean`

**Values**:
- `true`: Direction change is **allowed** (apply requested direction to snake)
- `false`: Direction change is **rejected** (keep current direction, ignore input)

**Semantics**:
- `true` returned for:
  - Perpendicular directions (90-degree turns): 8 combinations
  - Same direction (continue current): 4 combinations
- `false` returned for:
  - Reverse directions (180-degree turns): 4 combinations

**Guarantees**:
- Always returns Boolean (never throws exception)
- Deterministic (same inputs always produce same output)
- Pure function (no side effects, no state mutation)
- Fast (<1ms execution time)

## Behavioral Contract

### Validation Rules

| Current | Requested | Result | Reason |
|---------|-----------|--------|--------|
| UP | DOWN | `false` | Reverse direction (180°) |
| UP | LEFT | `true` | Perpendicular direction (90°) |
| UP | RIGHT | `true` | Perpendicular direction (90°) |
| UP | UP | `true` | Same direction (0°) |
| DOWN | UP | `false` | Reverse direction (180°) |
| DOWN | LEFT | `true` | Perpendicular direction (90°) |
| DOWN | RIGHT | `true` | Perpendicular direction (90°) |
| DOWN | DOWN | `true` | Same direction (0°) |
| LEFT | RIGHT | `false` | Reverse direction (180°) |
| LEFT | UP | `true` | Perpendicular direction (90°) |
| LEFT | DOWN | `true` | Perpendicular direction (90°) |
| LEFT | LEFT | `true` | Same direction (0°) |
| RIGHT | LEFT | `false` | Reverse direction (180°) |
| RIGHT | UP | `true` | Perpendicular direction (90°) |
| RIGHT | DOWN | `true` | Perpendicular direction (90°) |
| RIGHT | RIGHT | `true` | Same direction (0°) |

**Total Combinations**: 16 (4 current × 4 requested)
- **Valid** (return `true`): 12 combinations
- **Invalid** (return `false`): 4 combinations (reverse pairs)

### Invariants

1. **Symmetry**: If `validate(A, B) == false`, then `validate(B, A) == false`
   - Example: `validate(UP, DOWN) == false` ⟺ `validate(DOWN, UP) == false`

2. **Self-Acceptance**: `validate(D, D) == true` for all directions D
   - Example: `validate(UP, UP) == true`

3. **Perpendicular Count**: For each direction, exactly 2 requested directions are perpendicular
   - Example: For `UP`, perpendicular directions are `LEFT` and `RIGHT`

4. **Reverse Count**: For each direction, exactly 1 requested direction is reverse
   - Example: For `UP`, reverse direction is `DOWN`

5. **Completeness**: Every (current, requested) pair has a defined result (no undefined cases)

### Edge Cases

| Case | Input | Expected Output | Notes |
|------|-------|-----------------|-------|
| Initial direction | `(RIGHT, LEFT)` | `false` | Reverse of default starting direction |
| Same direction spam | `(UP, UP)` called 100x | `true` every time | Idempotent, no side effects |
| Rapid alternation | `(UP, LEFT)` then `(LEFT, UP)` | `true` both | Both perpendicular |
| All perpendicular | `(UP, LEFT)`, `(UP, RIGHT)` | `true` both | Both valid from UP |
| Reverse after perpendicular | `(UP, LEFT)` valid, then `(LEFT, RIGHT)` | Second is `false` | Reverse of new current |

## Implementation Constraints

### Performance Requirements

- **Latency**: Must complete in <1ms on minimum spec device (Android 7.0, 1GB RAM)
- **Allocation**: Minimal heap allocation (prefer primitive Boolean return, avoid objects)
- **Thread Safety**: Must be safe to call from UI thread (validation on input events)
- **Concurrency**: Must be stateless (no shared mutable state between invocations)

### Quality Requirements

- **Purity**: No side effects (doesn't modify parameters or global state)
- **Determinism**: Same inputs always produce same output
- **No Exceptions**: Never throws exceptions (all Direction combinations are valid inputs)
- **No Null**: Cannot receive null inputs (Direction enum is non-nullable)

### Testing Requirements

- **Unit Tests**: All 16 direction combinations tested with parameterized tests
- **Edge Cases**: Rapid invocation, same direction spam, alternating perpendicular
- **Performance**: Microbenchmark validates <1ms requirement
- **Contract Tests**: Verify invariants (symmetry, self-acceptance, etc.)

## Usage Examples

### Example 1: Basic Validation in ViewModel

```kotlin
class GameViewModel(
    private val validateDirection: ValidateDirectionUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState.initial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun handleDirectionInput(requestedDirection: Direction) {
        val currentDirection = _gameState.value.snake.direction

        // Validate direction change
        val isValid = validateDirection(currentDirection, requestedDirection)

        if (isValid) {
            // Apply direction change
            _gameState.update { state ->
                state.copy(
                    snake = state.snake.copy(direction = requestedDirection)
                )
            }
        }
        // Invalid directions silently ignored (per spec requirement)
    }
}
```

### Example 2: Integration with Swipe Detector

```kotlin
// In Composable UI
SwipeableGameBoard(
    onSwipe = { swipeDirection: Direction ->
        viewModel.handleDirectionInput(swipeDirection)
    }
)

// ViewModel handles validation
fun handleDirectionInput(requested: Direction) {
    val current = gameState.value.snake.direction

    if (validateDirection(current, requested)) {
        updateSnakeDirection(requested)
    }
}
```

### Example 3: Logging Rejected Directions (Debugging)

```kotlin
fun handleDirectionInput(requested: Direction) {
    val current = gameState.value.snake.direction
    val isValid = validateDirection(current, requested)

    if (!isValid) {
        Log.d("DirectionValidation", "Rejected: $current → $requested (reverse)")
    } else {
        updateSnakeDirection(requested)
    }
}
```

## Test Contract

### Unit Test Requirements

```kotlin
class ValidateDirectionUseCaseTest {

    private lateinit var useCase: ValidateDirectionUseCase

    @Before
    fun setup() {
        useCase = ValidateDirectionUseCaseImpl()
    }

    @Test
    fun `rejects all reverse directions`() {
        // UP ↔ DOWN
        assertFalse(useCase(Direction.UP, Direction.DOWN))
        assertFalse(useCase(Direction.DOWN, Direction.UP))

        // LEFT ↔ RIGHT
        assertFalse(useCase(Direction.LEFT, Direction.RIGHT))
        assertFalse(useCase(Direction.RIGHT, Direction.LEFT))
    }

    @Test
    fun `accepts all perpendicular directions`() {
        // From UP
        assertTrue(useCase(Direction.UP, Direction.LEFT))
        assertTrue(useCase(Direction.UP, Direction.RIGHT))

        // From DOWN
        assertTrue(useCase(Direction.DOWN, Direction.LEFT))
        assertTrue(useCase(Direction.DOWN, Direction.RIGHT))

        // From LEFT
        assertTrue(useCase(Direction.LEFT, Direction.UP))
        assertTrue(useCase(Direction.LEFT, Direction.DOWN))

        // From RIGHT
        assertTrue(useCase(Direction.RIGHT, Direction.UP))
        assertTrue(useCase(Direction.RIGHT, Direction.DOWN))
    }

    @Test
    fun `accepts same direction`() {
        Direction.values().forEach { direction ->
            assertTrue(useCase(direction, direction))
        }
    }

    @Test
    fun `validation is symmetric for reverse directions`() {
        assertTrue(
            useCase(Direction.UP, Direction.DOWN) == useCase(Direction.DOWN, Direction.UP)
        )
        assertTrue(
            useCase(Direction.LEFT, Direction.RIGHT) == useCase(Direction.RIGHT, Direction.LEFT)
        )
    }

    @Test
    fun `completes in under 1 millisecond`() {
        val iterations = 10000
        val startTime = System.nanoTime()

        repeat(iterations) {
            useCase(Direction.UP, Direction.DOWN)
        }

        val totalTime = (System.nanoTime() - startTime) / 1_000_000.0 // ms
        val averageTime = totalTime / iterations

        assertTrue(
            "Average validation time $averageTime ms exceeds 1ms threshold",
            averageTime < 1.0
        )
    }
}
```

## Dependencies

### Required

- `Direction` enum (from `domain/model/Direction.kt`)
  - Provides `UP`, `DOWN`, `LEFT`, `RIGHT` constants
  - Provides `reverse()` and `isReverse()` helper methods

### Optional

- `DirectionValidationResult` sealed class (if using detailed result contract)
- `RejectionReason` enum (if logging rejection reasons)

### No External Dependencies

- Pure Kotlin (no Android framework dependencies)
- No database, network, or file system access
- No coroutines or threading (synchronous operation)
- No dependency injection framework required (simple constructor injection)

## Integration Points

### Upstream (Callers)

- **GameViewModel**: Calls on every direction input from player
- **SwipeGestureDetector**: Indirectly triggers via ViewModel
- **Input Handler**: Any component that processes directional commands

### Downstream (Dependencies)

- **Direction Enum**: Uses `Direction.isReverse()` method for validation logic
- **No Other Dependencies**: Pure business logic with no side effects

### Data Flow

```
Player Swipes → SwipeDetector → Direction → ViewModel.handleInput()
                                                     ↓
                                          ValidateDirectionUseCase
                                                     ↓
                                               true / false
                                                     ↓
                                    ViewModel updates state OR ignores
```

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-01-18 | Initial contract definition for MVP |

## Future Considerations

**Potential Contract Extensions** (explicitly out of scope for MVP):

1. **Direction Buffering**: `validate(current, requested, buffered: Direction?)`
   - Would allow validating next queued direction
   - Requires state management for input queue

2. **Validation Context**: `validate(context: ValidationContext)`
   - Could include invincibility status, game mode, etc.
   - Adds complexity without clear current benefit

3. **Async Validation**: `suspend fun validate(...): Boolean`
   - Unnecessary for O(1) synchronous operation
   - Would complicate testing and usage

**Note**: Per constitution principle V (YAGNI), these extensions will only be considered if future requirements demand them.
