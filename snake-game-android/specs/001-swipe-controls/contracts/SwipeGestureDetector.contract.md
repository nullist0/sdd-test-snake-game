# Contract: SwipeGestureDetector

**Component Type**: Composable Modifier
**Package**: `com.snakegame.ui.game`
**Responsibility**: Detect swipe gestures and invoke callback with detected direction

## Interface

### Composable Function Signature

```kotlin
@Composable
fun Modifier.swipeGestureDetector(
    minSwipeDistance: Dp = SwipeGestureConfig.MIN_SWIPE_DISTANCE,
    onSwipe: (Direction) -> Unit
): Modifier
```

## Parameters

### Input Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `minSwipeDistance` | `Dp` | No | `SwipeGestureConfig.MIN_SWIPE_DISTANCE` (50.dp) | Minimum swipe distance required to trigger direction change. Swipes shorter than this are ignored. |
| `onSwipe` | `(Direction) -> Unit` | Yes | N/A | Callback invoked when valid swipe is detected with the calculated direction. |

### Return Value

| Type | Description |
|------|-------------|
| `Modifier` | A new Modifier that handles touch input and detects swipe gestures. Can be chained with other modifiers. |

## Behavior Specification

### Functional Behavior

**When applied to a Composable**:
1. Captures touch input events using `Modifier.pointerInput`
2. Tracks drag gestures using `detectDragGestures`
3. Accumulates drag deltas from `onDragStart` to `onDragEnd`
4. On drag end:
   - Calculates total swipe distance
   - If distance >= `minSwipeDistance`:
     - Determines direction using dominant axis algorithm
     - Invokes `onSwipe` callback with calculated direction
   - If distance < `minSwipeDistance`:
     - Silently ignores gesture (treats as accidental touch)

### Direction Calculation Algorithm

**Dominant Axis Approach**:
```
Given accumulated drag delta (x, y):

IF abs(x) > abs(y):
    // Horizontal swipe dominates
    IF x > 0:
        direction = Direction.RIGHT
    ELSE:
        direction = Direction.LEFT
ELSE:
    // Vertical swipe dominates
    IF y > 0:
        direction = Direction.DOWN
    ELSE:
        direction = Direction.UP
```

**Edge Cases**:
- **Exactly equal abs(x) == abs(y)**: Defaults to vertical direction (UP or DOWN)
- **Zero distance**: Ignored (never triggers callback)
- **Very short swipe**: Ignored if below `minSwipeDistance` threshold

## Dependencies

### Internal Dependencies

| Component | Type | Usage |
|-----------|------|-------|
| `SwipeGestureConfig.MIN_SWIPE_DISTANCE` | Configuration constant | Default value for minimum swipe threshold |
| `Direction` | Enum | Returned via `onSwipe` callback |
| `SwipeGesture` | Data class (optional) | May be used internally to encapsulate gesture data |
| `DetectSwipeDirectionUseCase` | Use case (recommended) | Encapsulates direction detection logic for testability |

### External Dependencies

| Component | Package | Usage |
|-----------|---------|-------|
| `Modifier.pointerInput` | `androidx.compose.ui.input.pointer` | Capture touch events |
| `detectDragGestures` | `androidx.compose.foundation.gestures` | Detect drag gestures |
| `Offset` | `androidx.compose.ui.geometry` | Represent touch positions and deltas |
| `LocalDensity` | `androidx.compose.ui.platform` | Convert Dp to pixels for threshold comparison |

## Contracts (Preconditions & Postconditions)

### Preconditions

- Modifier must be applied to a Composable that is rendered on screen
- `onSwipe` callback must be a stable reference (use `remember` or ViewModel method reference)
- Component is in active composition (not disposed)

### Postconditions

**On valid swipe (distance >= minSwipeDistance)**:
- `onSwipe` callback is invoked exactly once per gesture
- Direction passed to callback matches dominant axis of swipe
- Callback is invoked on drag end, not during drag

**On invalid swipe (distance < minSwipeDistance)**:
- `onSwipe` callback is NOT invoked
- No side effects occur

**On multi-touch or gesture cancellation**:
- Gesture is canceled, no callback invoked
- State is reset for next gesture

### Performance Guarantees

- **Latency**: Callback invoked within 50ms of drag end event
- **Frame rate**: Does not impact 60 FPS rendering (gesture detection is off main thread for heavy operations)
- **Memory**: No allocations during drag (except initial SwipeGesture object on drag end)

## Error Handling

### Error Conditions

| Condition | Behavior |
|-----------|----------|
| `onSwipe` callback throws exception | Exception propagates to caller; gesture state is reset |
| Modifier disposed mid-gesture | Gesture is canceled; `onDragCancel` cleans up state |
| Multiple simultaneous touches | First pointer is tracked; others are ignored |
| Negative `minSwipeDistance` | Treated as 0.dp (all swipes accepted) |

### Edge Case Handling

| Edge Case | Behavior |
|-----------|----------|
| Diagonal swipe (equal X and Y) | Defaults to vertical direction |
| Swipe starts outside bounds, ends inside | Gesture is tracked normally (uses relative delta) |
| Rapid consecutive swipes | Each swipe is processed independently; no queuing at this layer |
| Touch hold without movement | Ignored (delta = 0, below threshold) |

## Usage Examples

### Basic Usage

```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeGestureDetector { direction ->
                viewModel.handleDirectionInput(direction)
            }
    ) {
        // Game rendering
    }
}
```

### Custom Threshold

```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeGestureDetector(
                minSwipeDistance = 100.dp,  // Require longer swipes
                onSwipe = { direction ->
                    viewModel.handleDirectionInput(direction)
                }
            )
    ) {
        // Game rendering
    }
}
```

### Integration with State

```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeGestureDetector { direction ->
                // Only process swipes if game is active
                if (gameState.isPlaying) {
                    viewModel.handleDirectionInput(direction)
                }
            }
    ) {
        SnakeBoard(gameState)
    }
}
```

## Testing Contract

### Unit Tests (Compose UI Test)

**Test cases for SwipeGestureDetectorTest**:

```kotlin
@Test
fun swipeRight_aboveThreshold_invokesCallbackWithRightDirection() {
    // Given: modifier applied with callback
    // When: perform swipe right with distance > minSwipeDistance
    // Then: callback invoked with Direction.RIGHT
}

@Test
fun swipeLeft_aboveThreshold_invokesCallbackWithLeftDirection() {
    // Similar for LEFT, UP, DOWN
}

@Test
fun shortSwipe_belowThreshold_doesNotInvokeCallback() {
    // Given: minSwipeDistance = 50.dp
    // When: perform swipe with distance < 50.dp
    // Then: callback NOT invoked
}

@Test
fun diagonalSwipe_horizontalDominant_resolvesToHorizontalDirection() {
    // Given: swipe with deltaX=100, deltaY=50
    // When: gesture completes
    // Then: callback invoked with Direction.RIGHT (horizontal dominates)
}

@Test
fun diagonalSwipe_verticalDominant_resolvesToVerticalDirection() {
    // Given: swipe with deltaX=30, deltaY=100
    // When: gesture completes
    // Then: callback invoked with Direction.DOWN (vertical dominates)
}

@Test
fun rapidConsecutiveSwipes_eachInvokesCallback() {
    // Given: modifier applied
    // When: perform swipe right, then swipe up (rapid succession)
    // Then: callback invoked twice (once for RIGHT, once for UP)
}

@Test
fun multiTouch_firstPointerTracked_othersIgnored() {
    // Given: modifier applied
    // When: two fingers perform swipe
    // Then: only first pointer's swipe is processed
}
```

### Integration Tests

**Test cases for GameViewModel integration**:

```kotlin
@Test
fun swipeGestureDetector_withGameViewModel_updatesDirection() {
    // Given: GameScreen with ViewModel
    // When: perform swipe up gesture
    // Then: gameState.snake.direction becomes Direction.UP
}

@Test
fun swipeGestureDetector_reverseDirection_ignored() {
    // Given: snake moving RIGHT
    // When: perform swipe LEFT
    // Then: direction remains RIGHT (ValidateDirectionUseCase blocks it)
}
```

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-18 | Initial contract definition for swipe gesture detection |

## Related Contracts

- [DetectSwipeDirectionUseCase.contract.md](./DetectSwipeDirectionUseCase.contract.md) - Business logic for direction detection
- [GameViewModel.contract.md](./GameViewModel.contract.md) - Consumer of swipe events
