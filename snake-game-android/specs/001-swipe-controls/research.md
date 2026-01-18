# Research: Swipe-Based Snake Controls

**Feature**: 001-swipe-controls
**Date**: 2026-01-18
**Status**: Complete

## Overview

This document contains research findings for implementing swipe gesture detection in a Jetpack Compose-based snake game. The research focused on identifying the best Compose APIs, performance optimization strategies, and handling edge cases to meet the feature requirements of <100ms latency and 60 FPS performance.

## Research Questions Resolved

### 1. Best Compose API for Swipe Detection

**Decision**: Use `Modifier.pointerInput(Unit)` with `detectDragGestures`

**Rationale**:
- **Multi-directional support**: Unlike `draggable()` which only works in single orientation, `detectDragGestures` handles both X and Y axes simultaneously, which is essential for 4-directional snake control
- **Full gesture lifecycle**: Provides `onDragStart`, `onDrag`, and `onDragEnd` callbacks for complete gesture control
- **Built-in touch slop**: Automatically handles touch slop detection via `awaitTouchSlopOrCancellation`, preventing accidental triggers
- **Game-appropriate**: Gives raw `dragAmount` values (Offset) for precise direction calculation
- **Performance**: Efficient event-driven design, no continuous polling

**Alternatives Considered**:
- **`swipeable` modifier**: Rejected - deprecated since Compose 1.6.0-alpha01
- **`anchoredDraggable`**: Rejected - designed for UI patterns with discrete anchor points (like bottom sheets), overkill for game controls
- **`draggable()`**: Rejected - only supports single-axis gestures, not suitable for 4-directional control
- **Custom `PointerInputScope`**: Rejected - lower-level API requiring more boilerplate, no advantage over `detectDragGestures` for this use case

### 2. Swipe Direction Detection Algorithm

**Decision**: Magnitude comparison on dominant axis with cumulative drag tracking

**Algorithm**:
```kotlin
fun calculateSwipeDirection(dragAmount: Offset): Direction {
    val (x, y) = dragAmount
    // Compare absolute values to find dominant axis
    return when {
        abs(x) > abs(y) -> {
            // Horizontal swipe dominates
            if (x > 0) Direction.RIGHT else Direction.LEFT
        }
        else -> {
            // Vertical swipe dominates
            if (y > 0) Direction.DOWN else Direction.UP
        }
    }
}
```

**Rationale**:
- **Handles diagonal swipes naturally**: Whichever axis has greater magnitude wins
- **Simple and fast**: Single comparison, no trigonometry needed
- **Predictable behavior**: Players can intuitively understand which direction will be chosen
- **No angle calculation overhead**: More performant than atan2-based approaches

**Implementation Details**:
- Accumulate drag events in `onDrag` callback: `totalDrag += dragAmount`
- Evaluate direction in `onDragEnd` when gesture completes
- Reset accumulation on `onDragStart` for next gesture

### 3. Threshold Values

**Decision**: 50-100dp minimum swipe distance with built-in touch slop

**Minimum Swipe Distance**: **50-100dp** (density-independent pixels)
```kotlin
val minSwipeDistance = with(density) { 50.dp.toPx() }
```

**Rationale**:
- **Prevents accidental touches**: Requires intentional movement
- **Feels responsive**: Low enough to not feel laggy (50dp ≈ 3-4mm on most devices)
- **Platform consistency**: Aligns with Android gesture guidelines (48dp touch target minimum)
- **Validated by existing implementations**: Common threshold in production snake games

**Touch Slop Handling**:
- **Built-in**: `detectDragGestures` uses `awaitTouchSlopOrCancellation` internally
- **No custom implementation needed**: Platform handles it automatically
- **Typical value**: ~8dp on most devices (varies by device configuration)

**Debounce Time** (for rapid gesture prevention): **100-150ms**
```kotlin
var lastDirectionChange by remember { mutableLongStateOf(0L) }
val debounceTime = 100L // milliseconds

if (System.currentTimeMillis() - lastDirectionChange > debounceTime) {
    // Process direction change
    lastDirectionChange = System.currentTimeMillis()
}
```

**Rationale**:
- **Prevents input spam**: Snake can only change direction once per game tick
- **Matches human perception**: 100ms is imperceptible latency for most users
- **Satisfies FR-002**: Well below the 100ms requirement
- **Optional**: May not be needed if game loop already limits direction changes

### 4. Performance Optimizations for 60 FPS & <100ms Latency

**Frame Time Budget**: 16.67ms per frame (60 FPS) or 11ms per frame (120Hz displays)

**Key Optimizations**:

**A. Event Consumption**
```kotlin
detectDragGestures { change, dragAmount ->
    change.consume() // CRITICAL: Prevents event propagation and gesture conflicts
    // Process gesture
}
```
- **Rationale**: Prevents gesture events from bubbling to parent composables, avoiding double-processing

**B. Use `remember` for Gesture State**
```kotlin
var currentDirection by remember { mutableStateOf(Direction.RIGHT) }
var totalDrag by remember { mutableStateOf(Offset.Zero) }
```
- **Rationale**: Keeps state local to composition, survives recomposition, minimal overhead

**C. Minimize Recompositions**
```kotlin
// Use derivedStateOf for calculated values
val isGameOver by remember {
    derivedStateOf {
        gameState.snake.checkCollision()
    }
}
```
- **Rationale**: Only recomposes when derived value actually changes, not on every state update

**D. Lambda Modifiers for High-Frequency Updates**
```kotlin
// Good - defers state reads, no recomposition
.offset { IntOffset(snakeX.roundToInt(), snakeY.roundToInt()) }

// Bad - triggers recomposition on every snakeX/snakeY change
.offset(snakeX.dp, snakeY.dp)
```
- **Rationale**: Phase-based offset reading avoids unnecessary composition phase work

**E. Release Build Configuration**
```gradle
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            shrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}
```
- **Rationale**: R8 optimization critical for accurate performance; debug builds are 2-3x slower

**F. Baseline Profiles** (optional, for maximum performance)
- **Purpose**: Precompiles critical game loop code to reduce JIT overhead
- **Implementation**: Use Macrobenchmark to generate profiles
- **Impact**: ~30% improvement in startup and frame time for game code

### 5. Edge Case Handling

**Diagonal Swipes**:
- **Solution**: Dominant axis approach (magnitude comparison)
- **Behavior**: If `abs(x) > abs(y)`, treat as horizontal swipe; otherwise vertical
- **User Experience**: Predictable and intuitive - swipe in the direction you want, system picks closest cardinal direction

**Minimum Swipe Distance (Accidental Touches)**:
```kotlin
detectDragGestures(
    onDragStart = { accumulatedDrag = Offset.Zero },
    onDrag = { change, dragAmount ->
        change.consume()
        accumulatedDrag += dragAmount
    },
    onDragEnd = {
        if (accumulatedDrag.getDistance() >= minSwipeDistance) {
            updateDirection(calculateDirection(accumulatedDrag))
        }
        // Else: ignore short swipes (accidental touches)
    }
)
```
- **Rationale**: Requires intentional movement, filters out taps and jitter

**Reverse Direction Prevention** (integration with existing ValidateDirectionUseCase):
```kotlin
fun isValidDirectionChange(current: Direction, new: Direction): Boolean {
    return !current.isReverse(new) // Use existing Direction.isReverse() method
}

// In gesture handler:
if (isValidDirectionChange(currentDirection, newDirection)) {
    onDirectionChange(newDirection)
}
```
- **Rationale**: Leverages existing domain logic from ValidateDirectionUseCase

**Multi-Touch Handling**:
- **Built-in**: `detectDragGestures` automatically handles single-pointer gestures
- **Behavior**: If multiple fingers touch, first pointer is tracked, others ignored
- **Edge case**: Rapid finger switching handled gracefully by Compose

**Swipes Outside Play Area**:
- **Solution**: Apply `pointerInput` modifier to game canvas only (not entire screen)
- **Behavior**: Swipes outside game area don't trigger direction changes
- **Implementation**: Attach modifier to game Board composable, not root layout

## Implementation Architecture

### Component Design

**1. SwipeGesture (domain/model)**
```kotlin
data class SwipeGesture(
    val startPosition: Offset,
    val endPosition: Offset,
    val delta: Offset
) {
    fun getDistance(): Float = delta.getDistance()
}
```
- **Purpose**: Value object representing touch gesture data
- **Responsibility**: Calculate gesture metrics (distance, direction)

**2. DetectSwipeDirectionUseCase (domain/usecase)**
```kotlin
class DetectSwipeDirectionUseCase {
    operator fun invoke(gesture: SwipeGesture): Direction? {
        // Apply minimum distance threshold
        // Calculate direction from dominant axis
        // Return Direction or null if invalid
    }
}
```
- **Purpose**: Business logic for converting gestures to directions
- **Responsibility**: Apply thresholds, determine direction from gesture data
- **Testability**: Pure function, easy to unit test

**3. SwipeGestureDetector (ui/game)**
```kotlin
@Composable
fun Modifier.swipeGestureDetector(
    minSwipeDistance: Dp = 50.dp,
    onSwipe: (Direction) -> Unit
): Modifier {
    val density = LocalDensity.current
    // Implement detectDragGestures
    // Call DetectSwipeDirectionUseCase
    // Invoke onSwipe callback
}
```
- **Purpose**: Composable modifier for gesture capture
- **Responsibility**: Integrate Compose gesture APIs with domain logic
- **Reusability**: Can be applied to any composable needing swipe detection

**4. GameScreen Integration (ui/game)**
```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .swipeGestureDetector { direction ->
                viewModel.handleDirectionInput(direction)
            }
    ) {
        // Render snake, food, score
    }
}
```
- **Purpose**: Main game UI with gesture handling
- **Responsibility**: Coordinate gesture detection with ViewModel state updates

### Data Flow

```
User Touch → Compose Gesture System → SwipeGestureDetector modifier
    ↓
DetectSwipeDirectionUseCase (validate threshold, calculate direction)
    ↓
GameViewModel.handleDirectionInput(direction)
    ↓
ValidateDirectionUseCase (prevent reverse direction)
    ↓
Update GameState.snake.direction (StateFlow)
    ↓
GameScreen recomposes with new direction
```

## Testing Strategy

### Unit Tests

**SwipeGestureTest** (domain/model):
- Test `getDistance()` calculation
- Verify Offset handling

**DetectSwipeDirectionUseCaseTest** (domain/usecase):
- Test direction calculation for each cardinal direction
- Test diagonal swipes resolve to dominant axis
- Test minimum distance threshold rejection
- Test edge cases (zero distance, exactly equal X/Y)

**SwipeGestureDetectorTest** (ui/game - Compose UI Test):
- Test swipe gestures trigger `onSwipe` callback
- Test minimum distance threshold
- Test direction calculation integration
- Use `performTouchInput { swipe(start, end) }`

### Integration Tests

**GameViewModelTest** (existing, expand):
- Test swipe direction → state update flow
- Test reverse direction prevention via swipe
- Test rapid swipe handling (queue behavior if implemented)

### Manual Testing Checklist

- [ ] Swipe up/down/left/right all work correctly
- [ ] Reverse directions are blocked
- [ ] Short swipes (taps) don't change direction
- [ ] Diagonal swipes resolve to nearest direction
- [ ] Works on different screen sizes
- [ ] Performance: 60 FPS maintained during gameplay
- [ ] Latency: <100ms from swipe end to visual direction change

## Performance Validation

**Tools**:
- **Layout Inspector**: Check recomposition counts, verify skipping
- **Macrobenchmark**: Measure frame time, jank percentage
- **System Trace**: Record input latency from touch to frame

**Metrics to Track**:
- Frame time P50, P95, P99 (target: <16.67ms for 60 FPS)
- Input latency P50, P95 (target: <100ms per FR-002)
- Recomposition count (should be minimal during gesture handling)
- Memory allocations during gestures (should be zero or near-zero)

**Validation**:
```kotlin
// Use Android Studio Profiler
// Run Macrobenchmark test:
@Test
fun swipeGesturePerformance() {
    benchmarkRule.measureRepeated(
        packageName = "com.snakegame",
        metrics = listOf(FrameTimingMetric()),
        iterations = 10
    ) {
        // Perform swipe gestures
        // Verify frame timing meets 60 FPS target
    }
}
```

## References

### Official Android Documentation
- [Drag, swipe, and fling | Jetpack Compose](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/drag-swipe-fling)
- [Understand gestures | Jetpack Compose](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures)
- [Jetpack Compose Performance](https://developer.android.com/develop/ui/compose/performance)
- [Practical performance problem solving in Jetpack Compose](https://developer.android.com/codelabs/jetpack-compose-performance)

### Community Resources
- [The Complete Guide to detectDragGestures and pointerInput in Jetpack Compose](https://medium.com/@ramadan123sayed/the-complete-guide-to-detectdraggestures-and-pointerinput-in-jetpack-compose-08f7f367d9bc)
- [Detect Swipe Direction on Jetpack Compose](https://medium.com/backyard-programmers/detect-swipe-direction-on-jetpack-compose-to-trigger-bottomsheetscaffold-8d9116c8107c)
- [Gestures in Jetpack compose — All you need to know — Part 1](https://canopas.com/gestures-in-jetpack-compose-all-you-need-to-know-part-1-9d26570e56bb)
- [Gestures in Jetpack compose — All you need to know — Part 2](https://canopas.com/gestures-in-jetpack-compose-all-you-need-to-know-part-2-61da0c2bab6f)

### Implementation Examples
- [Create a Snake Game with Jetpack Compose](https://hariaguswidakdo.medium.com/create-a-snake-game-with-jetpack-compose-2fe9ffda9a44)
- [GitHub - FunnySaltyFish/JetpackComposeSnake](https://github.com/FunnySaltyFish/JetpackComposeSnake)
- [Simple Snake Game Jetpack Compose - Gist](https://gist.github.com/ardakazanci/9261953a1033553bd946838b92c61801)

## Summary

All NEEDS CLARIFICATION items from Technical Context have been resolved:

| Question | Decision | Confidence |
|----------|----------|------------|
| Best Compose gesture API? | `pointerInput` + `detectDragGestures` | High - official recommendation, proven in production |
| Direction detection algorithm? | Magnitude comparison on dominant axis | High - simple, fast, handles edge cases |
| Threshold values? | 50-100dp minimum swipe distance | High - validated by existing implementations |
| Performance approach? | Event-driven with state minimization | High - standard Compose best practices |
| Edge case handling? | Built-in touch slop + custom thresholds | High - covers all spec edge cases |

**Status**: Research complete. Ready for Phase 1 (design artifacts).
