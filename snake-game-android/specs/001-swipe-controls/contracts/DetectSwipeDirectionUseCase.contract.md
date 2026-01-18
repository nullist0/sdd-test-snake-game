# Contract: DetectSwipeDirectionUseCase

**Component Type**: Use Case (Business Logic)
**Package**: `com.snakegame.domain.usecase`
**Responsibility**: Convert SwipeGesture data into a cardinal Direction, applying threshold validation

## Interface

### Class Signature

```kotlin
class DetectSwipeDirectionUseCase {
    operator fun invoke(gesture: SwipeGesture): Direction?
}
```

## Parameters

### Input Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `gesture` | `SwipeGesture` | Yes | Swipe gesture data containing start position, end position, and delta |

### Return Value

| Type | Description | When Returned |
|------|-------------|---------------|
| `Direction?` | The detected cardinal direction (UP, DOWN, LEFT, RIGHT) | When swipe distance >= minimum threshold |
| `null` | No direction detected | When swipe distance < minimum threshold (accidental touch) |

## Behavior Specification

### Functional Behavior

**Algorithm**:
```
1. Calculate swipe distance from gesture.getDistance()
2. Check if distance >= SwipeGestureConfig.MIN_SWIPE_DISTANCE (converted to pixels)
3. If below threshold:
   - Return null (swipe too short, likely accidental)
4. If above threshold:
   - Compare abs(gesture.deltaX) vs abs(gesture.deltaY)
   - Determine dominant axis (horizontal vs vertical)
   - Determine direction based on sign of dominant component:
     - Horizontal: deltaX > 0 → RIGHT, deltaX < 0 → LEFT
     - Vertical: deltaY > 0 → DOWN, deltaY < 0 → UP
   - Return determined Direction
```

### Detailed Logic

**Direction Determination**:
```kotlin
fun invoke(gesture: SwipeGesture): Direction? {
    // Step 1: Threshold validation
    if (gesture.getDistance() < minSwipeDistancePx) {
        return null
    }

    // Step 2: Dominant axis calculation
    val deltaX = gesture.deltaX
    val deltaY = gesture.deltaY

    return when {
        // Horizontal dominates
        abs(deltaX) > abs(deltaY) -> {
            if (deltaX > 0) Direction.RIGHT else Direction.LEFT
        }
        // Vertical dominates (or equal - defaults to vertical)
        else -> {
            if (deltaY > 0) Direction.DOWN else Direction.UP
        }
    }
}
```

### Edge Cases

| Edge Case | Input | Expected Output | Rationale |
|-----------|-------|-----------------|-----------|
| Exactly equal deltaX and deltaY | `SwipeGesture(delta=Offset(50, 50))` | `Direction.DOWN` (or UP based on sign) | Default to vertical when ambiguous |
| Zero delta | `SwipeGesture(delta=Offset(0, 0))` | `null` | Distance is 0, below any threshold |
| Very small swipe | `SwipeGesture(delta=Offset(10, 5))` | `null` | Distance < MIN_SWIPE_DISTANCE |
| Perfect horizontal swipe | `SwipeGesture(delta=Offset(200, 0))` | `Direction.RIGHT` | deltaY=0, horizontal dominates |
| Perfect vertical swipe | `SwipeGesture(delta=Offset(0, -200))` | `Direction.UP` | deltaX=0, vertical dominates |
| Diagonal with horizontal bias | `SwipeGesture(delta=Offset(150, 80))` | `Direction.RIGHT` | abs(150) > abs(80) |
| Diagonal with vertical bias | `SwipeGesture(delta=Offset(60, -120))` | `Direction.UP` | abs(60) < abs(120) |

## Dependencies

### Internal Dependencies

| Component | Type | Usage |
|-----------|------|-------|
| `SwipeGesture` | Data class | Input parameter |
| `Direction` | Enum | Return type |
| `SwipeGestureConfig.MIN_SWIPE_DISTANCE` | Configuration | Threshold for validation |

### External Dependencies

| Component | Package | Usage |
|-----------|---------|-------|
| `Density` | `androidx.compose.ui.unit` | Convert Dp to pixels for threshold comparison (injected or accessed via context) |

## Contracts (Preconditions & Postconditions)

### Preconditions

- `gesture` must be non-null
- `gesture.startPosition` and `gesture.endPosition` must be valid Offset values
- Density context available for Dp-to-pixel conversion

### Postconditions

**When distance >= threshold**:
- Returns non-null Direction
- Direction matches dominant axis of gesture delta
- Direction is one of: UP, DOWN, LEFT, RIGHT

**When distance < threshold**:
- Returns null
- No side effects

### Invariants

- Pure function: same input always produces same output
- No state modification
- No I/O or side effects
- Thread-safe (can be called from any thread)

## Validation Rules

### Threshold Validation

| Rule | Implementation | Requirement Reference |
|------|----------------|----------------------|
| Minimum swipe distance | `gesture.getDistance() >= minSwipeDistancePx` | FR-004: System MUST ignore swipe inputs that do not meet minimum distance threshold |
| Direction ambiguity resolution | Vertical direction chosen when `abs(deltaX) == abs(deltaY)` | FR-005: System MUST determine swipe direction based on dominant axis |

## Usage Examples

### Basic Usage

```kotlin
val detectSwipeDirection = DetectSwipeDirectionUseCase()

val gesture = SwipeGesture(
    startPosition = Offset(100f, 200f),
    endPosition = Offset(100f, 50f)  // Swipe upward
)

val direction = detectSwipeDirection(gesture)
// Result: Direction.UP
```

### With Null Handling

```kotlin
val detectSwipeDirection = DetectSwipeDirectionUseCase()

val shortGesture = SwipeGesture(
    startPosition = Offset(100f, 200f),
    endPosition = Offset(110f, 205f)  // Very short swipe
)

val direction = detectSwipeDirection(shortGesture)
// Result: null (swipe too short)

if (direction != null) {
    handleDirectionChange(direction)
}
// Nothing happens (null is ignored)
```

### Integration with SwipeGestureDetector

```kotlin
@Composable
fun Modifier.swipeGestureDetector(
    minSwipeDistance: Dp = SwipeGestureConfig.MIN_SWIPE_DISTANCE,
    onSwipe: (Direction) -> Unit
): Modifier {
    val detectSwipeDirection = remember { DetectSwipeDirectionUseCase() }
    val density = LocalDensity.current

    var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }

    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = { accumulatedDrag = Offset.Zero },
            onDrag = { change, dragAmount ->
                change.consume()
                accumulatedDrag += dragAmount
            },
            onDragEnd = {
                val gesture = SwipeGesture(
                    startPosition = Offset.Zero,
                    endPosition = accumulatedDrag
                )

                val direction = detectSwipeDirection(gesture)
                if (direction != null) {
                    onSwipe(direction)
                }
            }
        )
    }
}
```

## Testing Contract

### Unit Tests

**Test cases for DetectSwipeDirectionUseCaseTest**:

```kotlin
class DetectSwipeDirectionUseCaseTest {
    private lateinit var useCase: DetectSwipeDirectionUseCase

    @Before
    fun setup() {
        useCase = DetectSwipeDirectionUseCase()
    }

    @Test
    fun `swipe right with sufficient distance returns RIGHT`() {
        // Given: horizontal swipe to the right, above threshold
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(200f, 0f)  // 200px right
        )

        // When
        val result = useCase(gesture)

        // Then
        assertEquals(Direction.RIGHT, result)
    }

    @Test
    fun `swipe left with sufficient distance returns LEFT`() {
        // Given: horizontal swipe to the left
        val gesture = SwipeGesture(
            startPosition = Offset(200f, 0f),
            endPosition = Offset(0f, 0f)  // 200px left
        )

        // When
        val result = useCase(gesture)

        // Then
        assertEquals(Direction.LEFT, result)
    }

    @Test
    fun `swipe up with sufficient distance returns UP`() {
        // Given: vertical swipe upward (negative Y)
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 200f),
            endPosition = Offset(0f, 0f)  // 200px up
        )

        // When
        val result = useCase(gesture)

        // Then
        assertEquals(Direction.UP, result)
    }

    @Test
    fun `swipe down with sufficient distance returns DOWN`() {
        // Given: vertical swipe downward
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(0f, 200f)  // 200px down
        )

        // When
        val result = useCase(gesture)

        // Then
        assertEquals(Direction.DOWN, result)
    }

    @Test
    fun `swipe below minimum distance returns null`() {
        // Given: very short swipe (e.g., 20px)
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(20f, 0f)  // Too short
        )

        // When
        val result = useCase(gesture)

        // Then
        assertNull(result)
    }

    @Test
    fun `diagonal swipe with horizontal dominance returns horizontal direction`() {
        // Given: diagonal swipe with stronger horizontal component
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(200f, 80f)  // More horizontal
        )

        // When
        val result = useCase(gesture)

        // Then
        assertEquals(Direction.RIGHT, result)
    }

    @Test
    fun `diagonal swipe with vertical dominance returns vertical direction`() {
        // Given: diagonal swipe with stronger vertical component
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(60f, 200f)  // More vertical
        )

        // When
        val result = useCase(gesture)

        // Then
        assertEquals(Direction.DOWN, result)
    }

    @Test
    fun `exactly equal deltaX and deltaY defaults to vertical direction`() {
        // Given: perfect diagonal (equal X and Y)
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(150f, 150f)  // Equal components
        )

        // When
        val result = useCase(gesture)

        // Then
        assertEquals(Direction.DOWN, result)  // Defaults to vertical
    }

    @Test
    fun `zero distance swipe returns null`() {
        // Given: no movement
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 100f),
            endPosition = Offset(100f, 100f)  // No delta
        )

        // When
        val result = useCase(gesture)

        // Then
        assertNull(result)
    }
}
```

### Property-Based Tests (Optional)

```kotlin
@Test
fun `all swipes above threshold return non-null direction`() {
    // For all gestures where distance >= MIN_SWIPE_DISTANCE:
    // Result must be non-null and valid Direction
}

@Test
fun `all swipes below threshold return null`() {
    // For all gestures where distance < MIN_SWIPE_DISTANCE:
    // Result must be null
}
```

## Performance Characteristics

| Aspect | Specification | Measurement |
|--------|--------------|-------------|
| Time Complexity | O(1) | Constant time (simple comparisons) |
| Space Complexity | O(1) | No allocations, no state |
| Execution Time | <1ms | Typically <100 microseconds |
| Thread Safety | Thread-safe | Pure function, no shared state |
| Memory Allocations | 0 per call | No objects created |

## Error Handling

### Error Conditions

| Condition | Behavior | Mitigation |
|-----------|----------|------------|
| Null gesture (Kotlin null safety prevents this) | Compile-time error | Use non-null types |
| Invalid Offset values (NaN, Infinity) | Returns based on comparison (may return unexpected direction) | Validate gesture input in SwipeGestureDetector layer |
| Density not available | Compile-time or runtime error if Dp conversion fails | Ensure Density is injected or available |

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-18 | Initial contract definition for swipe direction detection logic |

## Related Contracts

- [SwipeGestureDetector.contract.md](./SwipeGestureDetector.contract.md) - UI layer consumer of this use case
- [SwipeGesture.contract.md](./SwipeGesture.contract.md) - Input data model
- [ValidateDirectionUseCase.contract.md](./ValidateDirectionUseCase.contract.md) - Downstream validation logic
