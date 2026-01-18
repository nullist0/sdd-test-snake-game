# Data Model: No Reverse Direction Control

**Feature**: 009-no-reverse-direction
**Date**: 2026-01-18
**Phase**: 1 - Design

## Overview

This document defines the data structures and domain models required for implementing reverse direction prevention in the snake game. The model focuses on direction representation, validation logic, and integration with the snake entity.

## Domain Entities

### Direction

**Purpose**: Represents the four cardinal directions the snake can move in. Provides validation methods for determining direction relationships.

**Type**: Kotlin sealed enum

**Properties**:
- None (enum constants only: UP, DOWN, LEFT, RIGHT)

**Methods**:
```kotlin
fun reverse(): Direction
    Returns the opposite direction of this direction.
    UP ↔ DOWN, LEFT ↔ RIGHT

fun isReverse(other: Direction): Boolean
    Returns true if other is the reverse direction of this direction.
    Used for validation: should return false to allow direction change.

fun isPerpendicular(other: Direction): Boolean
    Returns true if other is perpendicular (90 degrees) to this direction.
    Perpendicular directions are always allowed.
```

**Validation Rules**:
- Must be one of exactly four values: UP, DOWN, LEFT, RIGHT
- Each direction has exactly one reverse direction
- Each direction has exactly two perpendicular directions
- Direction is immutable once created (enum constant)

**State Transitions**:
```
Current: UP
  ↓ Valid Changes
  ├─ LEFT (perpendicular) ✓
  ├─ RIGHT (perpendicular) ✓
  ├─ UP (same) ✓
  └─ DOWN (reverse) ✗ REJECTED

Current: DOWN
  ↓ Valid Changes
  ├─ LEFT (perpendicular) ✓
  ├─ RIGHT (perpendicular) ✓
  ├─ DOWN (same) ✓
  └─ UP (reverse) ✗ REJECTED

Current: LEFT
  ↓ Valid Changes
  ├─ UP (perpendicular) ✓
  ├─ DOWN (perpendicular) ✓
  ├─ LEFT (same) ✓
  └─ RIGHT (reverse) ✗ REJECTED

Current: RIGHT
  ↓ Valid Changes
  ├─ UP (perpendicular) ✓
  ├─ DOWN (perpendicular) ✓
  ├─ RIGHT (same) ✓
  └─ LEFT (reverse) ✗ REJECTED
```

**Relationships**:
- Used by: Snake entity (current direction)
- Used by: SwipeGestureDetector (requested direction)
- Used by: ValidateDirectionUseCase (validation logic)
- Used by: MoveSnakeUseCase (movement calculation)

**Example Kotlin Implementation**:
```kotlin
enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    fun reverse(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    fun isReverse(other: Direction): Boolean =
        this.reverse() == other

    fun isPerpendicular(other: Direction): Boolean =
        this != other && !isReverse(other)

    companion object {
        // Optional: vector representation for movement
        fun Direction.toVector(): Pair<Int, Int> = when (this) {
            UP -> Pair(0, -1)    // y decreases
            DOWN -> Pair(0, 1)   // y increases
            LEFT -> Pair(-1, 0)  // x decreases
            RIGHT -> Pair(1, 0)  // x increases
        }
    }
}
```

---

### DirectionValidationResult

**Purpose**: Encapsulates the result of direction validation, including whether the direction change is allowed and the reason if rejected.

**Type**: Kotlin sealed class (discriminated union)

**Variants**:

1. **Valid**
   - Indicates the direction change is allowed
   - Properties: `newDirection: Direction` (the validated direction to apply)

2. **Rejected**
   - Indicates the direction change is blocked
   - Properties: `reason: RejectionReason` (why it was rejected)

**RejectionReason enum**:
- `REVERSE_NOT_ALLOWED` - requested direction is reverse of current

**Validation Rules**:
- Only one variant can be active at a time
- Valid variant contains the direction to apply (may be same as current)
- Rejected variant prevents any direction change

**Relationships**:
- Returned by: ValidateDirectionUseCase
- Consumed by: GameViewModel (to apply or ignore direction change)

**Example Kotlin Implementation**:
```kotlin
sealed class DirectionValidationResult {
    data class Valid(val newDirection: Direction) : DirectionValidationResult()
    data class Rejected(val reason: RejectionReason) : DirectionValidationResult()
}

enum class RejectionReason {
    REVERSE_NOT_ALLOWED
}

// Simplified boolean result (alternative if detailed result not needed)
typealias IsDirectionValid = Boolean
```

**Note**: The spec requires silent rejection of invalid directions (no user feedback). The detailed `DirectionValidationResult` approach is optional - a simple `Boolean` return type may be sufficient for MVP. Choose based on whether you need to log rejection reasons for debugging.

---

### Snake (Extended)

**Purpose**: Represents the snake entity with position, body segments, and current movement direction. Extended to include direction as part of state.

**Type**: Kotlin data class

**Properties**:
```kotlin
data class Snake(
    val head: Position,
    val body: List<Position>,
    val direction: Direction  // [THIS FEATURE] Added for direction tracking
)
```

**Validation Rules**:
- `direction` must be one of four Direction enum values
- `direction` represents the current movement direction (last valid direction)
- Initial direction at game start should be set to a default (e.g., RIGHT)
- Direction only changes when validated input is applied

**State Management**:
- Direction is immutable in data class (create new Snake instance for direction change)
- Direction persists across game ticks (snake continues in same direction)
- Direction survives screen rotation (stored in ViewModel)

**Relationships**:
- Contains: Direction (current movement direction)
- Used by: ValidateDirectionUseCase (provides current direction for validation)
- Modified by: GameViewModel (creates new Snake with updated direction after validation)
- Used by: MoveSnakeUseCase (uses direction to calculate next head position)

**Initial State**:
```kotlin
fun Snake.Companion.initial(gridSize: Int): Snake {
    val centerX = gridSize / 2
    val centerY = gridSize / 2
    return Snake(
        head = Position(centerX, centerY),
        body = listOf(
            Position(centerX - 1, centerY),
            Position(centerX - 2, centerY),
            Position(centerX - 3, centerY)
        ),
        direction = Direction.RIGHT  // Default initial direction
    )
}
```

---

## Data Flow

### Direction Change Flow

```
User Swipes Screen
      ↓
SwipeGestureDetector
      ↓
Converts to Direction (UP/DOWN/LEFT/RIGHT)
      ↓
GameViewModel.handleDirectionInput(requestedDirection)
      ↓
ValidateDirectionUseCase(currentDirection, requestedDirection)
      ↓
   [Validation]
      ↓
  ┌───┴───┐
  ↓       ↓
Valid   Rejected
  ↓       ↓
Update  Ignore
Snake   (silent)
```

### Validation Logic Flow

```
ValidateDirectionUseCase(current, requested)
      ↓
current.isReverse(requested)?
      ↓
  ┌───┴───┐
  ↓       ↓
 YES      NO
  ↓       ↓
Reject  Accept
Return  Return
False   True
```

### State Update Flow

```
GameState.snake.direction = Current Direction
                                   ↓
                          [Validation Passes]
                                   ↓
                   Create New Snake with New Direction
                                   ↓
          GameState.copy(snake = updatedSnake)
                                   ↓
                     UI Observes State Change
                                   ↓
                      Next Game Tick: Move Snake
                      (uses new direction)
```

## Invariants

### Direction Invariants

1. **Uniqueness**: Each direction has exactly one reverse direction
2. **Symmetry**: If A is reverse of B, then B is reverse of A
3. **Completeness**: Every direction must define a reverse (no null cases)
4. **Perpendicular Count**: Each direction has exactly 2 perpendicular directions
5. **Self-Exclusion**: No direction is perpendicular to itself
6. **Transitivity**: If A is perpendicular to B, then A is not reverse of B

### Snake Direction Invariants

1. **Always Set**: Snake always has a current direction (never null)
2. **Valid Direction**: Snake direction is always one of four Direction enum values
3. **Consistency**: Direction matches last valid input (after validation)
4. **Persistence**: Direction does not change without explicit validated input
5. **Game Loop**: Snake moves in current direction every game tick

## Validation Examples

### Valid Direction Changes

| Current | Requested | Valid? | Reason |
|---------|-----------|--------|--------|
| UP | LEFT | ✓ | Perpendicular (90°) |
| UP | RIGHT | ✓ | Perpendicular (90°) |
| UP | UP | ✓ | Same direction (continue) |
| DOWN | LEFT | ✓ | Perpendicular (90°) |
| DOWN | RIGHT | ✓ | Perpendicular (90°) |
| LEFT | UP | ✓ | Perpendicular (90°) |
| LEFT | DOWN | ✓ | Perpendicular (90°) |
| RIGHT | UP | ✓ | Perpendicular (90°) |
| RIGHT | DOWN | ✓ | Perpendicular (90°) |

### Invalid Direction Changes (Rejected)

| Current | Requested | Valid? | Reason |
|---------|-----------|--------|--------|
| UP | DOWN | ✗ | Reverse (180°) - instant self-collision |
| DOWN | UP | ✗ | Reverse (180°) - instant self-collision |
| LEFT | RIGHT | ✗ | Reverse (180°) - instant self-collision |
| RIGHT | LEFT | ✗ | Reverse (180°) - instant self-collision |

## Integration Points

### With Swipe Controls (Feature 001)

**Input**: SwipeGestureDetector produces swipe direction (UP, DOWN, LEFT, RIGHT)
**This Feature**: Validates swipe direction against current snake direction
**Output**: Accepted or rejected direction change

**Contract**:
- SwipeGestureDetector must produce Direction enum (not raw coordinates)
- Validation happens after swipe-to-direction conversion
- Rejected directions are silently ignored (no feedback to swipe detector)

### With Collision Detection (Feature 005)

**Input**: Snake movement system uses validated direction
**This Feature**: Ensures direction cannot cause immediate self-collision
**Output**: Direction that prevents instant reverse into body

**Contract**:
- Validation prevents reverse direction before collision check runs
- Collision detection still checks self-collision for turning into body
- Reverse prevention is first line of defense, collision detection is second

### With Snake Movement

**Input**: Game loop tick triggers snake movement
**This Feature**: Provides validated current direction for movement calculation
**Output**: Direction used to calculate next head position

**Contract**:
- Movement system always uses `snake.direction` from validated state
- Direction does not change during movement calculation (immutable)
- New position calculated as: `head + direction.toVector()`

## Testing Data

### Test Fixtures

```kotlin
// Test fixture: All direction pairs
val allDirectionPairs = Direction.values().flatMap { current ->
    Direction.values().map { requested ->
        Pair(current, requested)
    }
}
// Total: 16 combinations (4 × 4)

// Test fixture: Valid direction changes
val validChanges = listOf(
    // Perpendicular (8 combinations)
    Direction.UP to Direction.LEFT,
    Direction.UP to Direction.RIGHT,
    Direction.DOWN to Direction.LEFT,
    Direction.DOWN to Direction.RIGHT,
    Direction.LEFT to Direction.UP,
    Direction.LEFT to Direction.DOWN,
    Direction.RIGHT to Direction.UP,
    Direction.RIGHT to Direction.DOWN,

    // Same direction (4 combinations)
    Direction.UP to Direction.UP,
    Direction.DOWN to Direction.DOWN,
    Direction.LEFT to Direction.LEFT,
    Direction.RIGHT to Direction.RIGHT,
)
// Total: 12 valid

// Test fixture: Invalid direction changes
val invalidChanges = listOf(
    Direction.UP to Direction.DOWN,
    Direction.DOWN to Direction.UP,
    Direction.LEFT to Direction.RIGHT,
    Direction.RIGHT to Direction.LEFT,
)
// Total: 4 invalid (reverse)
```

### Edge Cases to Test

1. **Initial direction**: Validate direction change from initial RIGHT direction
2. **Rapid inputs**: Multiple direction changes queued before game tick
3. **Same direction spam**: Repeatedly requesting current direction
4. **Alternating perpendicular**: UP → LEFT → UP → LEFT (rapid oscillation)
5. **Reverse attempt during turn**: Request reverse at exact moment of perpendicular turn
6. **Null/uninitialized**: Ensure direction is never null (type system prevents this)

## Performance Considerations

### Memory Usage

- **Direction enum**: 4 constants, ~32 bytes total
- **Snake.direction field**: 8 bytes (object reference)
- **Validation result**: 16-24 bytes (sealed class instance)
- **Total per game state**: <100 bytes overhead

### Computational Complexity

- **Direction.reverse()**: O(1) - simple when expression lookup
- **Direction.isReverse(other)**: O(1) - enum equality check
- **ValidateDirectionUseCase**: O(1) - single boolean comparison
- **Direction change in Snake**: O(1) - create new data class instance (immutable)

### Performance Targets

- **Validation latency**: <0.01ms (10 microseconds)
- **Memory allocation**: <1KB per validation (transient, GC collects immediately)
- **Frame impact**: 0% (validation on input event, not in game loop)

## Future Extensibility

### Potential Enhancements (Out of Scope for MVP)

1. **Diagonal Directions**: Extend enum to 8 directions (N, NE, E, SE, S, SW, W, NW)
   - Would require: updating reverse() to handle diagonals
   - Would require: redefining perpendicular (adjacent 45° angles)

2. **Direction History**: Track last N directions for undo/replay
   - Would require: `data class DirectionHistory(val history: List<Direction>)`
   - Would require: bounded queue to prevent unbounded growth

3. **Validation Feedback**: Provide haptic/audio feedback on rejected input
   - Would require: extending DirectionValidationResult with feedback field
   - Would require: UI layer to consume feedback and trigger vibration/sound

4. **Input Buffering**: Queue next direction change to apply after current move completes
   - Would require: `data class DirectionQueue(val pending: Direction?)`
   - Would require: validation of buffered direction on each game tick

**Note**: All enhancements are explicitly out of scope per constitution principle V (YAGNI).
