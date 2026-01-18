# Quickstart: Implementing Swipe-Based Snake Controls

**Feature**: 001-swipe-controls
**For**: Developers implementing this feature
**Estimated Time**: 3-4 hours (following TDD approach)

## Overview

This quickstart guide walks you through implementing swipe-based directional controls for the snake game using Test-Driven Development (TDD). You'll build gesture detection using Jetpack Compose APIs and integrate it with the existing game architecture.

## Prerequisites

- Android Studio installed (Arctic Fox or newer recommended)
- Kotlin 1.9.20+ configured
- Jetpack Compose dependencies already set up (✓ confirmed in build.gradle.kts)
- Existing Direction and ValidateDirectionUseCase code reviewed
- Constitution principles understood (especially Principle III: TDD)

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer                          │
│  ┌──────────────────┐      ┌──────────────────┐   │
│  │ GameScreen       │◄─────┤ SwipeGesture     │   │
│  │ (Composable)     │      │ Detector         │   │
│  └────────┬─────────┘      │ (Modifier)       │   │
│           │                └──────────────────┘   │
│           │ Direction                             │
│           ▼                                       │
│  ┌──────────────────┐                            │
│  │ GameViewModel    │                            │
│  │ (State Manager)  │                            │
│  └────────┬─────────┘                            │
└───────────┼──────────────────────────────────────┘
            │
            │ Direction (validated)
            ▼
┌─────────────────────────────────────────────────────┐
│                 Domain Layer                        │
│  ┌──────────────────────────┐                      │
│  │ DetectSwipeDirection     │                      │
│  │ UseCase                  │                      │
│  │ (SwipeGesture→Direction) │                      │
│  └──────────────────────────┘                      │
│                                                     │
│  ┌──────────────────────────┐                      │
│  │ ValidateDirection        │                      │
│  │ UseCase (existing)       │                      │
│  │ (prevent reverse)        │                      │
│  └──────────────────────────┘                      │
│                                                     │
│  ┌──────────────────────────┐                      │
│  │ Models: Direction,       │                      │
│  │ SwipeGesture, Config     │                      │
│  └──────────────────────────┘                      │
└─────────────────────────────────────────────────────┘
```

## Implementation Roadmap

### Phase 1: Domain Layer (Models & Use Cases) - 1 hour

1. **Create SwipeGestureConfig** (5 min)
2. **Create SwipeGesture model** + tests (20 min)
3. **Create DetectSwipeDirectionUseCase** + tests (35 min)

### Phase 2: UI Layer (Gesture Detection) - 1.5 hours

4. **Create SwipeGestureDetector modifier** + tests (60 min)
5. **Create GameScreen composable** + tests (30 min)

### Phase 3: Integration & Testing - 1 hour

6. **Integration tests** (30 min)
7. **Manual testing & refinement** (30 min)

---

## Step-by-Step Implementation

### Step 1: Create SwipeGestureConfig (5 min)

**File**: `app/src/main/java/com/snakegame/domain/model/SwipeGestureConfig.kt`

**TDD**: No test needed (configuration object)

**Code**:
```kotlin
package com.snakegame.domain.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object SwipeGestureConfig {
    val MIN_SWIPE_DISTANCE: Dp = 50.dp
    const val DEBOUNCE_TIME_MS: Long = 100L
    const val MAX_QUEUED_DIRECTIONS: Int = 0  // Start simple
}
```

**Verification**: Build succeeds, object is accessible

---

### Step 2: Create SwipeGesture Model (20 min)

#### 2.1 Write Tests FIRST (Red)

**File**: `app/src/test/java/com/snakegame/domain/model/SwipeGestureTest.kt`

```kotlin
package com.snakegame.domain.model

import androidx.compose.ui.geometry.Offset
import org.junit.Test
import kotlin.test.assertEquals

class SwipeGestureTest {

    @Test
    fun `delta calculates correctly from start to end position`() {
        // Given
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(150f, 250f)
        )

        // When
        val delta = gesture.delta

        // Then
        assertEquals(Offset(50f, 50f), delta)
    }

    @Test
    fun `getDistance calculates euclidean distance`() {
        // Given: 3-4-5 right triangle
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(3f, 4f)
        )

        // When
        val distance = gesture.getDistance()

        // Then
        assertEquals(5f, distance, 0.01f)
    }

    @Test
    fun `deltaX returns horizontal component`() {
        // Given
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(180f, 210f)
        )

        // When
        val deltaX = gesture.deltaX

        // Then
        assertEquals(80f, deltaX)
    }

    @Test
    fun `deltaY returns vertical component`() {
        // Given
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(110f, 350f)
        )

        // When
        val deltaY = gesture.deltaY

        // Then
        assertEquals(150f, deltaY)
    }

    @Test
    fun `negative delta for leftward swipe`() {
        // Given: swipe left
        val gesture = SwipeGesture(
            startPosition = Offset(200f, 100f),
            endPosition = Offset(50f, 100f)
        )

        // When
        val deltaX = gesture.deltaX

        // Then
        assertEquals(-150f, deltaX)
    }

    @Test
    fun `negative delta for upward swipe`() {
        // Given: swipe up (negative Y in Compose)
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(100f, 50f)
        )

        // When
        val deltaY = gesture.deltaY

        // Then
        assertEquals(-150f, deltaY)
    }
}
```

**Run tests**: They should FAIL (class doesn't exist yet)

#### 2.2 Implement SwipeGesture (Green)

**File**: `app/src/main/java/com/snakegame/domain/model/SwipeGesture.kt`

```kotlin
package com.snakegame.domain.model

import androidx.compose.ui.geometry.Offset

data class SwipeGesture(
    val startPosition: Offset,
    val endPosition: Offset
) {
    val delta: Offset
        get() = endPosition - startPosition

    fun getDistance(): Float = delta.getDistance()

    val deltaX: Float
        get() = delta.x

    val deltaY: Float
        get() = delta.y
}
```

**Run tests**: They should PASS ✓

---

### Step 3: Create DetectSwipeDirectionUseCase (35 min)

#### 3.1 Write Tests FIRST (Red)

**File**: `app/src/test/java/com/snakegame/domain/usecase/DetectSwipeDirectionUseCaseTest.kt`

```kotlin
package com.snakegame.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.SwipeGesture
import com.snakegame.domain.model.SwipeGestureConfig
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetectSwipeDirectionUseCaseTest {

    private lateinit var useCase: DetectSwipeDirectionUseCase
    private val density = Density(2f)  // 2x density for testing

    @Before
    fun setup() {
        useCase = DetectSwipeDirectionUseCase(density)
    }

    @Test
    fun `swipe right above threshold returns RIGHT`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(200f, 0f)  // Pure horizontal right
        )

        val result = useCase(gesture)

        assertEquals(Direction.RIGHT, result)
    }

    @Test
    fun `swipe left above threshold returns LEFT`() {
        val gesture = SwipeGesture(
            startPosition = Offset(200f, 0f),
            endPosition = Offset(0f, 0f)  // Pure horizontal left
        )

        val result = useCase(gesture)

        assertEquals(Direction.LEFT, result)
    }

    @Test
    fun `swipe up above threshold returns UP`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 200f),
            endPosition = Offset(0f, 0f)  // Pure vertical up (negative Y)
        )

        val result = useCase(gesture)

        assertEquals(Direction.UP, result)
    }

    @Test
    fun `swipe down above threshold returns DOWN`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(0f, 200f)  // Pure vertical down
        )

        val result = useCase(gesture)

        assertEquals(Direction.DOWN, result)
    }

    @Test
    fun `swipe below minimum distance returns null`() {
        // MIN_SWIPE_DISTANCE = 50.dp * 2 (density) = 100px
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(50f, 0f)  // Only 50px, below threshold
        )

        val result = useCase(gesture)

        assertNull(result)
    }

    @Test
    fun `diagonal swipe with horizontal dominance returns horizontal direction`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(200f, 80f)  // More horizontal (200 vs 80)
        )

        val result = useCase(gesture)

        assertEquals(Direction.RIGHT, result)
    }

    @Test
    fun `diagonal swipe with vertical dominance returns vertical direction`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(60f, 200f)  // More vertical (200 vs 60)
        )

        val result = useCase(gesture)

        assertEquals(Direction.DOWN, result)
    }

    @Test
    fun `exactly equal deltaX and deltaY defaults to vertical`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(150f, 150f)  // Perfect diagonal
        )

        val result = useCase(gesture)

        assertEquals(Direction.DOWN, result)  // Defaults to vertical
    }

    @Test
    fun `zero distance returns null`() {
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 100f),
            endPosition = Offset(100f, 100f)  // No movement
        )

        val result = useCase(gesture)

        assertNull(result)
    }
}
```

**Run tests**: They should FAIL ✓

#### 3.2 Implement DetectSwipeDirectionUseCase (Green)

**File**: `app/src/main/java/com/snakegame/domain/usecase/DetectSwipeDirectionUseCase.kt`

```kotlin
package com.snakegame.domain.usecase

import androidx.compose.ui.unit.Density
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.SwipeGesture
import com.snakegame.domain.model.SwipeGestureConfig
import kotlin.math.abs

class DetectSwipeDirectionUseCase(
    private val density: Density
) {
    operator fun invoke(gesture: SwipeGesture): Direction? {
        // Convert min distance from Dp to pixels
        val minDistancePx = with(density) {
            SwipeGestureConfig.MIN_SWIPE_DISTANCE.toPx()
        }

        // Check threshold
        if (gesture.getDistance() < minDistancePx) {
            return null
        }

        // Determine direction based on dominant axis
        val deltaX = gesture.deltaX
        val deltaY = gesture.deltaY

        return when {
            abs(deltaX) > abs(deltaY) -> {
                // Horizontal dominates
                if (deltaX > 0) Direction.RIGHT else Direction.LEFT
            }
            else -> {
                // Vertical dominates (or equal - default to vertical)
                if (deltaY > 0) Direction.DOWN else Direction.UP
            }
        }
    }
}
```

**Run tests**: They should PASS ✓

---

### Step 4: Create SwipeGestureDetector Modifier (60 min)

#### 4.1 Write Tests FIRST (Red) - Compose UI Test

**File**: `app/src/androidTest/java/com/snakegame/ui/game/SwipeGestureDetectorTest.kt`

```kotlin
package com.snakegame.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.snakegame.domain.model.Direction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class SwipeGestureDetectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun swipeRight_invokesCallbackWithRightDirection() {
        var capturedDirection: Direction? = null

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                    }
            )
        }

        // Perform swipe right
        composeTestRule.onNodeWithTag("game-board", useUnmergedTree = true)
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(x = center.x + 300f),  // Swipe right
                    durationMillis = 200
                )
            }

        // Verify
        assertEquals(Direction.RIGHT, capturedDirection)
    }

    // Similar tests for LEFT, UP, DOWN, short swipes, diagonals...
}
```

**Note**: For full Compose UI tests, you may need to add a test tag. Alternatively, use `performTouchInput` on a known composable.

**Run tests**: They should FAIL ✓

#### 4.2 Implement SwipeGestureDetector (Green)

**File**: `app/src/main/java/com/snakegame/ui/game/SwipeGestureDetector.kt`

```kotlin
package com.snakegame.ui.game

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.SwipeGesture
import com.snakegame.domain.model.SwipeGestureConfig
import com.snakegame.domain.usecase.DetectSwipeDirectionUseCase

@Composable
fun Modifier.swipeGestureDetector(
    minSwipeDistance: Dp = SwipeGestureConfig.MIN_SWIPE_DISTANCE,
    onSwipe: (Direction) -> Unit
): Modifier {
    val density = LocalDensity.current
    val detectSwipeDirection = remember { DetectSwipeDirectionUseCase(density) }

    var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }

    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                accumulatedDrag = Offset.Zero
            },
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

**Run tests**: They should PASS ✓

---

### Step 5: Create GameScreen Composable (30 min)

#### 5.1 Write Integration Test FIRST (Red)

**File**: `app/src/androidTest/java/com/snakegame/ui/game/GameScreenTest.kt`

```kotlin
package com.snakegame.ui.game

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.snakegame.domain.model.Direction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun swipeGesture_updatesViewModelDirection() {
        val viewModel = GameViewModel()

        composeTestRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        // Initial direction should be RIGHT (or whatever default is)
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)

        // Perform swipe up
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(y = center.y - 300f),  // Swipe up
                    durationMillis = 200
                )
            }

        // Verify direction changed to UP
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)
    }
}
```

**Run test**: It should FAIL ✓

#### 5.2 Implement GameScreen (Green)

**File**: `app/src/main/java/com/snakegame/ui/game/GameScreen.kt`

```kotlin
package com.snakegame.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .swipeGestureDetector { direction ->
                viewModel.handleDirectionInput(direction)
            }
    ) {
        // TODO: Render snake, food, score, etc.
        // For now, just a basic container with gesture detection
    }
}
```

**Run test**: It should PASS ✓

---

### Step 6: Integration Testing (30 min)

Run all existing tests to ensure no regressions:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

**Expected Results**:
- All existing Direction tests pass ✓
- All existing ValidateDirectionUseCase tests pass ✓
- All existing GameViewModel tests pass ✓
- All new SwipeGesture tests pass ✓
- All new DetectSwipeDirectionUseCase tests pass ✓
- All new SwipeGestureDetector tests pass ✓
- All new GameScreen tests pass ✓

---

### Step 7: Manual Testing (30 min)

1. **Build and install on device/emulator**:
   ```bash
   ./gradlew installDebug
   ```

2. **Test scenarios** (from spec acceptance criteria):

   **User Story 1: Directional Control**:
   - [ ] Swipe up → snake moves up
   - [ ] Swipe down → snake moves down
   - [ ] Swipe left → snake moves left
   - [ ] Swipe right → snake moves right

   **User Story 2: Prevent Reverse Direction**:
   - [ ] Moving right, swipe left → snake continues right (blocked)
   - [ ] Moving up, swipe down → snake continues up (blocked)
   - [ ] Moving down, swipe up → snake continues down (blocked)
   - [ ] Moving left, swipe right → snake continues left (blocked)

   **User Story 3: Responsive Swipe Detection**:
   - [ ] Swipe feels responsive (<100ms perceived latency)
   - [ ] Short taps don't change direction (below threshold)
   - [ ] Diagonal swipes resolve to nearest direction
   - [ ] Game maintains 60 FPS during swipes

3. **Performance verification**:
   - Enable "Profile GPU Rendering" in Developer Options
   - Verify green bars stay below 16ms line (60 FPS)
   - Perform rapid swipes and verify no frame drops

---

## Troubleshooting

### Tests Failing

**Issue**: SwipeGestureDetector tests fail with "No node found"
**Solution**: Ensure Box has `.testTag("game-board")` or use `.onRoot()` for touch input

**Issue**: Direction not updating in GameViewModel test
**Solution**: Use `runTest { }` from `kotlinx-coroutines-test` for coroutine testing

### Performance Issues

**Issue**: Frame drops during swipe gestures
**Solution**: Verify `change.consume()` is called in `onDrag` to prevent event propagation

**Issue**: Direction updates feel laggy
**Solution**: Check debounce logic, reduce `DEBOUNCE_TIME_MS` if needed

### Gesture Detection Issues

**Issue**: Short swipes are triggering direction changes
**Solution**: Increase `MIN_SWIPE_DISTANCE` threshold

**Issue**: Diagonal swipes feel unpredictable
**Solution**: Review dominant axis logic, ensure `abs(deltaX) > abs(deltaY)` is correct

---

## Next Steps

After completing this quickstart:

1. **Code Review**: Have team review implementation
2. **Refine Thresholds**: Tune `MIN_SWIPE_DISTANCE` based on user testing
3. **Add Visual Feedback**: Consider adding swipe trail or direction indicator
4. **Performance Benchmarking**: Run Macrobenchmark tests to validate 60 FPS
5. **Accessibility**: Consider adding alternative control schemes (out of scope for this feature, but note for future)

---

## Checklist

**Before Starting**:
- [ ] Read spec.md (understand requirements)
- [ ] Read research.md (understand technical approach)
- [ ] Read data-model.md (understand entities)
- [ ] Read contract files (understand interfaces)

**During Implementation**:
- [ ] Follow TDD: Red → Green → Refactor
- [ ] Write tests before implementation
- [ ] Run tests frequently
- [ ] Commit after each working component

**After Implementation**:
- [ ] All tests passing (unit + integration + manual)
- [ ] Code reviewed
- [ ] Performance validated (60 FPS, <100ms latency)
- [ ] No constitution violations

---

## Time Estimates

| Phase | Task | Estimated Time | Actual Time |
|-------|------|----------------|-------------|
| 1 | SwipeGestureConfig | 5 min | ___ |
| 1 | SwipeGesture + tests | 20 min | ___ |
| 1 | DetectSwipeDirectionUseCase + tests | 35 min | ___ |
| 2 | SwipeGestureDetector + tests | 60 min | ___ |
| 2 | GameScreen + tests | 30 min | ___ |
| 3 | Integration tests | 30 min | ___ |
| 3 | Manual testing | 30 min | ___ |
| **Total** | | **3.5 hours** | ___ |

**Note**: Times are estimates. Actual time may vary based on experience and environment setup.

---

## Support

**Questions?**
- Review contracts in `/specs/001-swipe-controls/contracts/`
- Check research findings in `research.md`
- Consult constitution for principles

**Stuck?**
- Run `./gradlew test` to identify failing tests
- Use Android Studio debugger to step through gesture detection
- Check Logcat for any exceptions

---

**Good luck with implementation! Remember: Red → Green → Refactor!** 🧪
