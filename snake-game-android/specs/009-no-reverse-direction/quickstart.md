# Quick Start: No Reverse Direction Control

**Feature**: 009-no-reverse-direction
**Audience**: Developers implementing this feature
**Date**: 2026-01-18
**Estimated Implementation Time**: 2-4 hours (following TDD)

## Overview

This guide walks you through implementing reverse direction prevention for the snake game using Test-Driven Development (TDD). You'll implement a validation layer that blocks 180-degree turns, preventing instant self-collision.

**Core Concept**: Snake can only turn perpendicular (90° left/right) or continue straight. Reverse (180°) turns are blocked.

**TDD Approach**: Write tests first (RED), implement to pass (GREEN), refactor for quality (REFACTOR).

## Prerequisites

### Knowledge Required

- Kotlin basics (enums, data classes, when expressions)
- Android fundamentals (ViewModel, StateFlow)
- JUnit testing basics
- Understanding of Clean Architecture (UI → Domain → Data layers)

### Tools & Environment

- Android Studio Hedgehog (2023.1.1) or later
- Kotlin 1.9+
- Android SDK 24+ (minimum), SDK 34 (target and compile)
- JUnit 4 or 5 for unit tests
- Gradle 8.2+ with Kotlin DSL

### Project Setup

If starting fresh, initialize Android project:

```bash
# Via Android Studio:
# File → New → New Project → Empty Activity
# - Language: Kotlin
# - Minimum SDK: API 24 (Nougat)
# - Build configuration language: Kotlin DSL

# Or via command line:
# (Use Android Studio GUI - command line setup is complex)
```

Ensure `build.gradle.kts` (app module) includes:

```kotlin
android {
    namespace = "com.snakegame"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        targetSdk = 34
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
}
```

## Step-by-Step Implementation (TDD)

### Step 1: Create Direction Enum (Test First)

**Duration**: 15 minutes

#### 1.1 Write Tests (RED Phase)

Create `app/src/test/java/com/snakegame/domain/model/DirectionTest.kt`:

```kotlin
package com.snakegame.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DirectionTest {

    @Test
    fun `reverse returns opposite direction for all directions`() {
        assertEquals(Direction.DOWN, Direction.UP.reverse())
        assertEquals(Direction.UP, Direction.DOWN.reverse())
        assertEquals(Direction.RIGHT, Direction.LEFT.reverse())
        assertEquals(Direction.LEFT, Direction.RIGHT.reverse())
    }

    @Test
    fun `isReverse returns true for opposite directions`() {
        assertTrue(Direction.UP.isReverse(Direction.DOWN))
        assertTrue(Direction.DOWN.isReverse(Direction.UP))
        assertTrue(Direction.LEFT.isReverse(Direction.RIGHT))
        assertTrue(Direction.RIGHT.isReverse(Direction.LEFT))
    }

    @Test
    fun `isReverse returns false for perpendicular directions`() {
        assertFalse(Direction.UP.isReverse(Direction.LEFT))
        assertFalse(Direction.UP.isReverse(Direction.RIGHT))
        assertFalse(Direction.DOWN.isReverse(Direction.LEFT))
        assertFalse(Direction.DOWN.isReverse(Direction.RIGHT))
    }

    @Test
    fun `isReverse returns false for same direction`() {
        Direction.values().forEach { direction ->
            assertFalse(direction.isReverse(direction))
        }
    }

    @Test
    fun `isPerpendicular returns true for 90 degree directions`() {
        // From UP
        assertTrue(Direction.UP.isPerpendicular(Direction.LEFT))
        assertTrue(Direction.UP.isPerpendicular(Direction.RIGHT))

        // From LEFT
        assertTrue(Direction.LEFT.isPerpendicular(Direction.UP))
        assertTrue(Direction.LEFT.isPerpendicular(Direction.DOWN))
    }

    @Test
    fun `isPerpendicular returns false for reverse and same directions`() {
        // Reverse
        assertFalse(Direction.UP.isPerpendicular(Direction.DOWN))

        // Same
        assertFalse(Direction.UP.isPerpendicular(Direction.UP))
    }
}
```

**Run tests**: They should FAIL (Direction class doesn't exist yet). This is expected in TDD.

```bash
./gradlew test
# Expected: FAILED - Direction class not found
```

#### 1.2 Implement Direction Enum (GREEN Phase)

Create `app/src/main/java/com/snakegame/domain/model/Direction.kt`:

```kotlin
package com.snakegame.domain.model

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
}
```

**Run tests again**: They should PASS now.

```bash
./gradlew test
# Expected: PASSED - All Direction tests green
```

#### 1.3 Refactor (REFACTOR Phase)

Direction enum is already clean. No refactoring needed for this simple case.

**TDD Checkpoint**: ✅ Direction enum complete with tests

---

### Step 2: Create Validation Use Case (Test First)

**Duration**: 30 minutes

#### 2.1 Write Parameterized Tests (RED Phase)

Create `app/src/test/java/com/snakegame/domain/usecase/ValidateDirectionUseCaseTest.kt`:

```kotlin
package com.snakegame.domain.usecase

import com.snakegame.domain.model.Direction
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class ValidateDirectionUseCaseTest(
    private val current: Direction,
    private val requested: Direction,
    private val expected: Boolean
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "current={0}, requested={1}, valid={2}")
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

    private val useCase = ValidateDirectionUseCase()

    @Test
    fun `validates direction change according to reverse rule`() {
        val result = useCase(current, requested)
        assertEquals(expected, result, "Validation failed for $current → $requested")
    }
}
```

**Run tests**: Should FAIL (ValidateDirectionUseCase doesn't exist yet).

```bash
./gradlew test
# Expected: FAILED - ValidateDirectionUseCase not found
```

#### 2.2 Implement Use Case (GREEN Phase)

Create `app/src/main/java/com/snakegame/domain/usecase/ValidateDirectionUseCase.kt`:

```kotlin
package com.snakegame.domain.usecase

import com.snakegame.domain.model.Direction

/**
 * Validates direction changes according to snake game rules.
 *
 * Rule: Snake cannot reverse direction (180-degree turn).
 * Reverse directions are rejected to prevent instant self-collision.
 */
class ValidateDirectionUseCase {
    /**
     * @param current The snake's current movement direction
     * @param requested The direction requested by player input
     * @return true if direction change is allowed, false if rejected
     */
    operator fun invoke(current: Direction, requested: Direction): Boolean {
        // Reject reverse directions, accept all others (perpendicular + same)
        return !current.isReverse(requested)
    }
}
```

**Run tests**: Should PASS (all 16 combinations covered).

```bash
./gradlew test
# Expected: PASSED - All 16 direction validation tests green
```

#### 2.3 Refactor (REFACTOR Phase)

Implementation is already minimal and clear. Consider adding documentation.

**TDD Checkpoint**: ✅ Validation use case complete with comprehensive tests

---

### Step 3: Integrate with ViewModel

**Duration**: 45 minutes

#### 3.1 Create Game State Models

Create `app/src/main/java/com/snakegame/domain/model/Position.kt`:

```kotlin
package com.snakegame.domain.model

data class Position(val x: Int, val y: Int)
```

Create `app/src/main/java/com/snakegame/domain/model/Snake.kt`:

```kotlin
package com.snakegame.domain.model

data class Snake(
    val head: Position,
    val body: List<Position>,
    val direction: Direction
) {
    companion object {
        fun initial(gridSize: Int = 15): Snake {
            val centerX = gridSize / 2
            val centerY = gridSize / 2
            return Snake(
                head = Position(centerX, centerY),
                body = listOf(
                    Position(centerX - 1, centerY),
                    Position(centerX - 2, centerY),
                    Position(centerX - 3, centerY)
                ),
                direction = Direction.RIGHT  // Default starting direction
            )
        }
    }
}
```

#### 3.2 Create ViewModel with Validation

Create `app/src/main/java/com/snakegame/ui/game/GameViewModel.kt`:

```kotlin
package com.snakegame.ui.game

import androidx.lifecycle.ViewModel
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.Snake
import com.snakegame.domain.usecase.ValidateDirectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameState(
    val snake: Snake,
    val score: Int = 0,
    val isGameOver: Boolean = false
) {
    companion object {
        fun initial(gridSize: Int = 15) = GameState(
            snake = Snake.initial(gridSize)
        )
    }
}

class GameViewModel : ViewModel() {

    private val validateDirection = ValidateDirectionUseCase()

    private val _gameState = MutableStateFlow(GameState.initial())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /**
     * Handles directional input from player (e.g., swipe gesture).
     * Validates the direction change and updates snake direction if valid.
     * Invalid directions (reverse) are silently ignored.
     */
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
}
```

#### 3.3 Write ViewModel Integration Test

Create `app/src/test/java/com/snakegame/ui/game/GameViewModelTest.kt`:

```kotlin
package com.snakegame.ui.game

import com.snakegame.domain.model.Direction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GameViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setup() {
        viewModel = GameViewModel()
    }

    @Test
    fun `initial snake direction is RIGHT`() {
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `accepts perpendicular direction change from initial`() {
        // Initial direction is RIGHT, UP is perpendicular
        viewModel.handleDirectionInput(Direction.UP)

        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `rejects reverse direction change from initial`() {
        // Initial direction is RIGHT, LEFT is reverse
        viewModel.handleDirectionInput(Direction.LEFT)

        // Direction should remain RIGHT (rejected)
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `accepts same direction input`() {
        // Initial direction is RIGHT, requesting RIGHT again
        viewModel.handleDirectionInput(Direction.RIGHT)

        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `multiple valid direction changes work correctly`() {
        // RIGHT → UP (perpendicular, valid)
        viewModel.handleDirectionInput(Direction.UP)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        // UP → LEFT (perpendicular, valid)
        viewModel.handleDirectionInput(Direction.LEFT)
        assertEquals(Direction.LEFT, viewModel.gameState.value.snake.direction)

        // LEFT → DOWN (perpendicular, valid)
        viewModel.handleDirectionInput(Direction.DOWN)
        assertEquals(Direction.DOWN, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `reverse direction attempt after valid turn is rejected`() {
        // RIGHT → UP (valid)
        viewModel.handleDirectionInput(Direction.UP)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        // UP → DOWN (reverse, invalid)
        viewModel.handleDirectionInput(Direction.DOWN)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)  // Still UP
    }
}
```

**Run tests**:

```bash
./gradlew test
# Expected: All tests PASS
```

**TDD Checkpoint**: ✅ ViewModel integration complete with tests

---

### Step 4: Add UI Integration (Simplified)

**Duration**: 30-60 minutes (depending on UI complexity)

#### 4.1 Create Simple Game Screen (Compose)

Create `app/src/main/java/com/snakegame/ui/game/GameScreen.kt`:

```kotlin
package com.snakegame.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snakegame.domain.model.Direction
import kotlin.math.abs

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    // Convert drag to direction
                    val direction = when {
                        abs(dragAmount.x) > abs(dragAmount.y) -> {
                            if (dragAmount.x > 0) Direction.RIGHT else Direction.LEFT
                        }
                        else -> {
                            if (dragAmount.y > 0) Direction.DOWN else Direction.UP
                        }
                    }

                    viewModel.handleDirectionInput(direction)
                }
            }
    ) {
        // Display current direction for debugging
        Text(
            text = "Direction: ${gameState.snake.direction}",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        // TODO: Render snake and grid (out of scope for this feature)
        // This feature only implements direction validation logic
    }
}
```

**Note**: Full game rendering (snake body, grid, fruit) is out of scope for this feature. This minimal UI demonstrates direction input integration.

---

## Testing Checklist

### Unit Tests ✅

- [x] Direction enum reverse() method
- [x] Direction enum isReverse() method
- [x] Direction enum isPerpendicular() method
- [x] ValidateDirectionUseCase all 16 combinations
- [x] ViewModel initial state
- [x] ViewModel accepts perpendicular directions
- [x] ViewModel rejects reverse directions
- [x] ViewModel accepts same direction
- [x] ViewModel multiple direction changes

### Integration Tests (Optional for MVP)

- [ ] Swipe gesture → direction validation flow
- [ ] Direction change persists across screen rotation
- [ ] Rapid swipe sequences handled correctly

### Manual Testing

- [ ] Swipe up, down, left, right - observe direction changes
- [ ] Attempt reverse direction - verify it's ignored
- [ ] Perpendicular turns work smoothly
- [ ] Same direction swipe continues current direction

## Performance Validation

Run microbenchmark to verify <1ms validation:

```kotlin
@Test
fun `validation completes in under 1 millisecond`() {
    val useCase = ValidateDirectionUseCase()
    val iterations = 10000

    val startTime = System.nanoTime()
    repeat(iterations) {
        useCase(Direction.UP, Direction.DOWN)
    }
    val endTime = System.nanoTime()

    val averageTimeMs = (endTime - startTime) / iterations / 1_000_000.0
    assertTrue(averageTimeMs < 1.0, "Average time: $averageTimeMs ms")
}
```

**Expected Result**: ~0.001ms (1 microsecond) per validation

## Common Issues & Solutions

### Issue 1: Tests Not Found

**Symptom**: Gradle can't find test classes

**Solution**:
```bash
# Rebuild project
./gradlew clean build

# Verify test source sets configured in build.gradle.kts
sourceSets {
    getByName("test") {
        java.srcDir("src/test/java")
    }
}
```

### Issue 2: Direction Enum Not Found in Tests

**Symptom**: Import errors for Direction in test files

**Solution**: Ensure Direction.kt is in `src/main/java/...` not `src/test/java/...`

### Issue 3: Validation Always Returns True

**Symptom**: Reverse directions accepted incorrectly

**Solution**: Check `isReverse()` implementation - should use `reverse() == other`, not `this == other`

### Issue 4: ViewModel State Not Updating

**Symptom**: Direction changes don't reflect in UI

**Solution**: Ensure StateFlow is collected in Composable:
```kotlin
val gameState by viewModel.gameState.collectAsState()
// Use gameState.snake.direction in UI
```

## Next Steps

### Immediate (This Feature)

1. ✅ Direction enum with validation methods
2. ✅ ValidateDirectionUseCase with comprehensive tests
3. ✅ ViewModel integration
4. ✅ Basic UI integration (swipe detection)

### Future Features (Separate Implementations)

- **001-swipe-controls**: Enhanced swipe gesture detection
- **005-collision-game-over**: Collision detection (complements reverse prevention)
- **007-snake-init-victory**: Snake initialization and victory conditions
- Game loop for continuous snake movement
- Rendering system for snake and grid visualization

## Additional Resources

### Documentation

- [Specification](./spec.md) - Feature requirements and user scenarios
- [Research](./research.md) - Technical decisions and rationale
- [Data Model](./data-model.md) - Domain entities and validation rules
- [Contract](./contracts/ValidateDirectionUseCase.kt.md) - Use case interface specification

### Kotlin/Android References

- [Kotlin Enums](https://kotlinlang.org/docs/enum-classes.html)
- [StateFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [JUnit Parameterized Tests](https://github.com/junit-team/junit4/wiki/parameterized-tests)

## Estimated Timeline

| Task | Duration | Notes |
|------|----------|-------|
| Direction enum + tests | 15 min | Simple enum with 3 methods |
| Validation use case + tests | 30 min | 16 parameterized test cases |
| ViewModel integration + tests | 45 min | State management and flow setup |
| UI integration (basic) | 30-60 min | Swipe gesture detection |
| Manual testing & debugging | 30 min | Verify on emulator/device |
| **Total** | **2.5-4 hours** | Following TDD strictly |

**Tip**: If ahead of schedule, add integration tests. If behind, focus on unit tests (ViewModel tests can be optional for MVP).

## Success Criteria

### All Tests Passing ✅

```bash
./gradlew test
# Expected output:
# BUILD SUCCESSFUL
# Tests: 25+ passed, 0 failed, 0 skipped
```

### Manual Validation ✅

- Swipe upward → snake turns up (if not already moving down)
- Swipe downward → ignored if moving up (reverse)
- Swipe left/right → snake turns (if perpendicular to current)
- Rapid swipes handled gracefully (no crashes)

### Performance ✅

- Validation <1ms (microbenchmark confirms)
- No frame drops during direction changes (60 FPS maintained)

---

**You're Ready!** Follow the TDD steps above, and you'll have reverse direction prevention working in 2-4 hours. Remember: **Red → Green → Refactor** for each component.
