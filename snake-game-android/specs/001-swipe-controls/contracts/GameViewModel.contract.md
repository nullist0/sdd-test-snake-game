# Contract: GameViewModel (Swipe Controls Extension)

**Component Type**: ViewModel (State Manager)
**Package**: `com.snakegame.ui.game`
**Responsibility**: Manage game state and handle direction input from swipe gestures

## Interface

### Existing Method (No Changes Required)

```kotlin
class GameViewModel : ViewModel() {
    val gameState: StateFlow<GameState>

    fun handleDirectionInput(requestedDirection: Direction)
}
```

## Methods

### handleDirectionInput

**Purpose**: Process directional input from swipe gestures (or other input sources) and update game state if valid.

#### Signature

```kotlin
fun handleDirectionInput(requestedDirection: Direction)
```

#### Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `requestedDirection` | `Direction` | Yes | The direction requested by player input (from swipe gesture) |

#### Return Value

| Type | Description |
|------|-------------|
| `Unit` | No return value. Updates internal state via StateFlow. |

## Behavior Specification

### Functional Behavior

**Algorithm**:
```
1. Read current snake direction from gameState.value.snake.direction
2. Validate requested direction using ValidateDirectionUseCase:
   - Check if requested direction is reverse of current direction
3. If validation passes (not reverse):
   - Update gameState by creating new state with updated snake direction
   - Emit new state via StateFlow
4. If validation fails (reverse direction):
   - Silently ignore input (no state change)
   - No error thrown, no logging (per spec FR-003)
```

### Detailed Logic

```kotlin
fun handleDirectionInput(requestedDirection: Direction) {
    val currentDirection = _gameState.value.snake.direction

    // Validate direction change using use case
    val isValid = validateDirection(currentDirection, requestedDirection)

    if (isValid) {
        // Apply validated direction change
        _gameState.update { state ->
            state.copy(
                snake = state.snake.copy(direction = requestedDirection)
            )
        }
    }
    // Invalid directions silently ignored (per spec requirement FR-003)
}
```

## Integration with Swipe Controls

### Data Flow

```
User Swipe Gesture
    ↓
SwipeGestureDetector (UI layer)
    ↓
DetectSwipeDirectionUseCase (domain layer)
    ↓ Direction?
GameViewModel.handleDirectionInput(direction) ← if non-null
    ↓
ValidateDirectionUseCase (domain layer)
    ↓ Boolean (valid?)
Update GameState.snake.direction ← if valid
    ↓
StateFlow emits new state
    ↓
GameScreen recomposes
```

### Call Site Example

```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeGestureDetector { direction ->
                // Direction from swipe is passed directly to ViewModel
                viewModel.handleDirectionInput(direction)
            }
    ) {
        // Game rendering
    }
}
```

## Dependencies

### Internal Dependencies

| Component | Type | Usage |
|-----------|------|-------|
| `ValidateDirectionUseCase` | Use case | Validates if direction change is allowed (prevents reverse) |
| `Direction` | Enum | Input parameter type |
| `GameState` | Data class | State container updated by this method |
| `Snake` | Data class | Entity whose direction is updated |

### External Dependencies

| Component | Package | Usage |
|-----------|---------|-------|
| `StateFlow` | `kotlinx.coroutines.flow` | Reactive state management |
| `MutableStateFlow.update` | `kotlinx.coroutines.flow` | Atomic state updates |
| `ViewModel` | `androidx.lifecycle` | Lifecycle-aware state holder |

## Contracts (Preconditions & Postconditions)

### Preconditions

- ViewModel is active (not cleared)
- GameState is initialized
- ValidateDirectionUseCase is initialized
- `requestedDirection` is a valid Direction enum value

### Postconditions

**When direction change is valid**:
- `gameState.value.snake.direction` is updated to `requestedDirection`
- StateFlow emits new GameState
- All StateFlow collectors receive updated state
- Update happens atomically (thread-safe)

**When direction change is invalid (reverse)**:
- `gameState.value.snake.direction` remains unchanged
- No StateFlow emission occurs (no unnecessary recomposition)
- No exceptions thrown
- No side effects

### Thread Safety

- Method can be called from any thread (typically main/UI thread)
- State updates are atomic via `MutableStateFlow.update`
- No race conditions between concurrent direction inputs
- Latest direction wins if multiple inputs occur simultaneously

## Validation Rules

### Direction Change Validation

| Rule | Implementation | Requirement Reference |
|------|----------------|----------------------|
| Prevent reverse direction | `ValidateDirectionUseCase(current, requested)` returns `false` for reverse | FR-003: System MUST prevent reverse-direction commands |
| Allow perpendicular direction | ValidateDirectionUseCase returns `true` for 90° turns | Implicit in FR-003 (only reverse is blocked) |
| Allow same direction | ValidateDirectionUseCase returns `true` for same direction | Allows continuous movement |

### Edge Cases

| Edge Case | Behavior | Rationale |
|-----------|----------|-----------|
| Rapid consecutive inputs (same direction) | All inputs processed, but state only updates if direction differs | Idempotent updates (StateFlow deduplicates) |
| Rapid consecutive inputs (alternating valid) | Each input updates state | Responsive controls |
| Input during game over | Processed normally (may need guard if not desired) | Consider adding game state check if needed |
| Input before game start | Processed normally (changes initial direction) | Allows pre-game direction setup |

## Performance Guarantees

| Aspect | Specification | Measurement |
|--------|--------------|-------------|
| Execution Time | <10ms (target <5ms) | Must not block UI thread |
| Memory Allocations | 1 GameState copy per valid input | Standard Kotlin data class copy |
| StateFlow Emission | <1ms | Coroutine dispatch overhead |
| Thread Safety | Always thread-safe | MutableStateFlow guarantees |

## Testing Contract

### Unit Tests

**Test cases for GameViewModelTest**:

```kotlin
class GameViewModelTest {
    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
    }

    @Test
    fun `handleDirectionInput with perpendicular direction updates state`() = runTest {
        // Given: snake moving RIGHT
        val initialState = viewModel.gameState.value
        assertEquals(Direction.RIGHT, initialState.snake.direction)

        // When: request UP (perpendicular)
        viewModel.handleDirectionInput(Direction.UP)

        // Then: direction updated
        val updatedState = viewModel.gameState.value
        assertEquals(Direction.UP, updatedState.snake.direction)
    }

    @Test
    fun `handleDirectionInput with reverse direction is ignored`() = runTest {
        // Given: snake moving RIGHT
        val initialState = viewModel.gameState.value
        assertEquals(Direction.RIGHT, initialState.snake.direction)

        // When: request LEFT (reverse)
        viewModel.handleDirectionInput(Direction.LEFT)

        // Then: direction unchanged
        val updatedState = viewModel.gameState.value
        assertEquals(Direction.RIGHT, updatedState.snake.direction)
    }

    @Test
    fun `handleDirectionInput with same direction is allowed`() = runTest {
        // Given: snake moving RIGHT
        val initialState = viewModel.gameState.value
        assertEquals(Direction.RIGHT, initialState.snake.direction)

        // When: request RIGHT (same)
        viewModel.handleDirectionInput(Direction.RIGHT)

        // Then: direction remains RIGHT (idempotent)
        val updatedState = viewModel.gameState.value
        assertEquals(Direction.RIGHT, updatedState.snake.direction)
    }

    @Test
    fun `handleDirectionInput from UP to LEFT is allowed`() = runTest {
        // Given: snake moving UP
        viewModel.handleDirectionInput(Direction.UP)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        // When: request LEFT (perpendicular)
        viewModel.handleDirectionInput(Direction.LEFT)

        // Then: direction updated
        assertEquals(Direction.LEFT, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `handleDirectionInput from UP to DOWN is blocked`() = runTest {
        // Given: snake moving UP
        viewModel.handleDirectionInput(Direction.UP)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        // When: request DOWN (reverse)
        viewModel.handleDirectionInput(Direction.DOWN)

        // Then: direction unchanged
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `rapid consecutive valid inputs all update state`() = runTest {
        // Given: snake moving RIGHT
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)

        // When: rapid inputs UP → LEFT → DOWN
        viewModel.handleDirectionInput(Direction.UP)
        viewModel.handleDirectionInput(Direction.LEFT)
        viewModel.handleDirectionInput(Direction.DOWN)

        // Then: final direction is DOWN (all valid, last wins)
        assertEquals(Direction.DOWN, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `StateFlow emits when direction changes`() = runTest {
        // Given: collector on gameState
        val emissions = mutableListOf<GameState>()
        val job = launch {
            viewModel.gameState.collect { emissions.add(it) }
        }

        // When: change direction
        viewModel.handleDirectionInput(Direction.UP)
        advanceUntilIdle()

        // Then: new state emitted
        assertTrue(emissions.size >= 2)  // Initial + update
        assertEquals(Direction.UP, emissions.last().snake.direction)

        job.cancel()
    }

    @Test
    fun `StateFlow does not emit when direction change is blocked`() = runTest {
        // Given: collector on gameState, snake moving RIGHT
        val emissions = mutableListOf<GameState>()
        val job = launch {
            viewModel.gameState.collect { emissions.add(it) }
        }
        advanceUntilIdle()
        val initialEmissionCount = emissions.size

        // When: attempt reverse direction
        viewModel.handleDirectionInput(Direction.LEFT)
        advanceUntilIdle()

        // Then: no new emission (state unchanged)
        assertEquals(initialEmissionCount, emissions.size)

        job.cancel()
    }
}
```

### Integration Tests

```kotlin
@Test
fun `swipe gesture to ViewModel integration updates direction`() {
    // Given: GameScreen with swipe detector
    // When: perform swipe up gesture
    // Then: gameState.snake.direction becomes UP
}

@Test
fun `swipe gesture with reverse direction is blocked`() {
    // Given: snake moving right
    // When: perform swipe left
    // Then: direction remains right (blocked by ValidateDirectionUseCase)
}
```

## State Management

### GameState Structure

```kotlin
data class GameState(
    val snake: Snake,
    // ... other fields
)

data class Snake(
    val direction: Direction,
    val segments: List<Position>,
    // ... other fields
)
```

### State Update Pattern

```kotlin
// Atomic update using StateFlow.update
_gameState.update { currentState ->
    currentState.copy(
        snake = currentState.snake.copy(
            direction = newDirection
        )
    )
}
```

**Benefits**:
- Immutable state updates (functional approach)
- Thread-safe via StateFlow atomicity
- Automatic deduplication (if state doesn't change, no emission)
- Testable (pure transformations)

## Error Handling

### Error Conditions

| Condition | Behavior | Mitigation |
|-----------|----------|------------|
| Invalid Direction enum value | Cannot occur (Kotlin enum safety) | Type system prevents |
| ValidateDirectionUseCase throws exception | Exception propagates, state remains unchanged | Ensure use case is exception-safe |
| StateFlow.update throws exception | State remains in previous consistent state | Kotlin coroutines guarantee |
| ViewModel cleared mid-update | Update completes normally; future calls ignored | Lifecycle framework handles |

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-18 | Contract documentation for existing GameViewModel.handleDirectionInput method in context of swipe controls |

## Related Contracts

- [SwipeGestureDetector.contract.md](./SwipeGestureDetector.contract.md) - UI layer input source
- [DetectSwipeDirectionUseCase.contract.md](./DetectSwipeDirectionUseCase.contract.md) - Converts gestures to directions
- [ValidateDirectionUseCase.contract.md](./ValidateDirectionUseCase.contract.md) - Validates direction changes (prevents reverse)

## Notes

**Existing Implementation**: This method already exists in the codebase and works correctly. This contract documents its behavior in the context of the swipe controls feature. No code changes are required to GameViewModel for swipe control integration.

**Extension Points** (potential future enhancements):
- Input queue for rapid swipes (FR-006) - currently not implemented
- Debouncing logic - currently handled by gesture detection layer
- Game state guards (e.g., ignore input during game over) - to be determined during implementation
