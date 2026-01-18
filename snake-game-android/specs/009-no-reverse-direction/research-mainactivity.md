# Phase 0: Research - MainActivity Entry Point for Snake Game

**Feature**: MainActivity Entry Point
**Date**: 2026-01-18
**Status**: Complete

## Overview

This document consolidates research findings and design decisions for implementing MainActivity as the entry point for the snake game using Jetpack Compose.

## Design Decisions

### 1. MainActivity + Compose Integration

**Decision**: Use `ComponentActivity` with `setContent` for Compose entry point

**Rationale**:
- `ComponentActivity` is the base class for Compose integration
- `setContent` is the standard way to launch Compose UI from an Activity
- Provides automatic lifecycle management for composables
- ViewModel integration is seamless with `viewModel()` composable function

**Implementation Pattern**:
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SnakeGameTheme {
                GameScreen()
            }
        }
    }
}
```

**Alternatives Considered**:
- **AppCompatActivity with ComposeView**: More complex, requires manual XML layout integration
- **Fragment with ComposeView**: Unnecessary indirection for single-screen game

**Lifecycle Management**:
- Compose automatically handles configuration changes (rotation)
- GameViewModel survives configuration changes via ViewModelProvider
- Game state preserved in ViewModel's StateFlow

---

### 2. Compose Canvas Rendering for Game Grid

**Decision**: Use `Canvas` composable with custom drawing for game grid

**Rationale**:
- **Performance**: Canvas provides direct drawing API with better control
- **Flexibility**: Can draw custom shapes, colors, and animations
- **Efficiency**: Single Canvas draw pass more efficient than 225 individual composables
- **Game-appropriate**: Canvas is designed for custom graphics like game grids

**Implementation Pattern**:
```kotlin
@Composable
fun SnakeGrid(
    gameState: GameState,
    gridSize: Int = 15,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cellSize = size.width / gridSize

        // Draw grid background
        drawRect(
            color = GameBackground,
            size = size
        )

        // Draw grid lines
        for (i in 0..gridSize) {
            val offset = i * cellSize
            // Vertical lines
            drawLine(
                color = GridLine,
                start = Offset(offset, 0f),
                end = Offset(offset, size.height),
                strokeWidth = 1f
            )
            // Horizontal lines
            drawLine(
                color = GridLine,
                start = Offset(0f, offset),
                end = Offset(size.width, offset),
                strokeWidth = 1f
            )
        }

        // Draw snake body
        gameState.snake.body.forEach { position ->
            drawRect(
                color = SnakeBody,
                topLeft = Offset(
                    position.x * cellSize,
                    position.y * cellSize
                ),
                size = Size(cellSize, cellSize)
            )
        }

        // Draw snake head
        drawRect(
            color = SnakeHead,
            topLeft = Offset(
                gameState.snake.head.x * cellSize,
                gameState.snake.head.y * cellSize
            ),
            size = Size(cellSize, cellSize)
        )
    }
}
```

**Alternatives Considered**:
- **LazyGrid with Box items**: Would create 225 composables with excessive recomposition
- **Custom Layout with drawBehind**: More complex without performance benefits

**Performance Optimization**:
- Use `remember` to cache cell size calculations
- Use `derivedStateOf` for computed grid properties
- Minimize recomposition by using `collectAsState()` only at top level

---

### 3. Swipe Gesture Detection in Compose

**Decision**: Use `Modifier.pointerInput` with `detectDragGestures` for swipe detection

**Rationale**:
- Native Compose gesture API (no third-party libraries)
- `detectDragGestures` provides drag delta for swipe calculation
- Simple threshold-based direction detection
- Low latency (<100ms from touch to direction change)

**Implementation Pattern**:
```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    // Convert drag delta to direction
                    val direction = when {
                        abs(dragAmount.x) > abs(dragAmount.y) -> {
                            // Horizontal swipe
                            if (dragAmount.x > 0) Direction.RIGHT else Direction.LEFT
                        }
                        else -> {
                            // Vertical swipe
                            if (dragAmount.y > 0) Direction.DOWN else Direction.UP
                        }
                    }

                    viewModel.handleDirectionInput(direction)
                }
            }
    ) {
        SnakeGrid(
            gameState = gameState,
            gridSize = 15,
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

**Alternatives Considered**:
- **Accompanist Swipeable**: Deprecated library, not recommended
- **Custom touch event handling**: More complex, reinvents wheel
- **Button-based D-pad**: Less intuitive for mobile game

**Threshold Values**:
- No minimum swipe distance required (immediate response)
- Direction determined by axis with larger delta
- Diagonal swipes resolve to dominant axis

**Gesture Sensitivity**:
- Uses relative drag amounts (works on all screen sizes)
- No calibration needed
- Responsive to small swipes

---

### 4. Theme and Color Management

**Decision**: Material3 theme with custom game colors

**Rationale**:
- Material3 is Android's modern design system
- Provides consistent color scheme and typography
- Easy to customize with game-specific colors
- Built-in support for light/dark themes

**Color Scheme**:
```kotlin
// ui/theme/Color.kt
val SnakeHead = Color(0xFF4CAF50)     // Vibrant green
val SnakeBody = Color(0xFF81C784)     // Light green
val GridLine = Color(0xFF424242)      // Dark gray
val GameBackground = Color(0xFF212121) // Near black

// Material3 theme colors (for future UI elements)
val Primary = Color(0xFF4CAF50)
val Secondary = Color(0xFF81C784)
val Background = Color(0xFF121212)
val Surface = Color(0xFF212121)
```

**Theme Setup**:
```kotlin
// ui/theme/Theme.kt
@Composable
fun SnakeGameTheme(
    darkTheme: Boolean = true,  // Game uses dark theme by default
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = Primary,
        secondary = Secondary,
        background = Background,
        surface = Surface
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Alternatives Considered**:
- **Custom theme without Material3**: More work, loses Material components
- **Light theme**: Dark theme is better for games (less eye strain)

---

### 5. Screen Size Responsiveness

**Decision**: Use `BoxWithConstraints` for responsive grid sizing

**Rationale**:
- `BoxWithConstraints` provides actual screen dimensions
- Calculate cell size dynamically based on screen size
- Maintain square cells for consistent gameplay
- Adapts to different device sizes automatically

**Implementation Pattern**:
```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val gridSize = 15
        val screenSize = min(maxWidth, maxHeight)
        val cellSize = screenSize / gridSize

        SnakeGrid(
            gameState = gameState,
            gridSize = gridSize,
            modifier = Modifier
                .size(screenSize)
                .align(Alignment.Center)
                .pointerInput(Unit) { /* swipe gestures */ }
        )
    }
}
```

**Responsive Behavior**:
- Grid uses minimum of screen width/height (maintains square)
- Centers grid on screen
- Cell size calculated as: `screenSize / 15`
- Works on phones, tablets, and foldables

**Alternatives Considered**:
- **Fixed pixel sizes**: Doesn't adapt to screen sizes
- **Density-independent pixels (dp)**: Still needs screen size awareness

---

## Best Practices Research

### Jetpack Compose for Games

**Key Findings**:
1. **Performance**: Canvas is efficient for 60 FPS rendering
2. **State Management**: Use StateFlow with `collectAsState()` for reactive UI
3. **Recomposition**: Minimize by using `remember`, `derivedStateOf`, and stable data classes
4. **Gestures**: `pointerInput` is low-level but powerful for custom gestures

**Game Loop Pattern**:
```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val gameState by viewModel.gameState.collectAsState()

    // Game loop using LaunchedEffect
    LaunchedEffect(Unit) {
        while (isActive) {
            delay(200)  // Game tick interval (5 ticks/second)
            viewModel.updateGame()
        }
    }

    // UI rendering
    SnakeGrid(gameState = gameState)
}
```

**Note**: Game loop implementation is out of scope for MainActivity entry point (will be added in future feature).

---

### Android Lifecycle Integration

**Key Findings**:
1. **Compose Lifecycle**: Composables automatically pause when activity pauses
2. **ViewModel Scope**: StateFlow survives configuration changes
3. **Game Pause**: `LaunchedEffect` automatically cancels on composition leave

**Activity Lifecycle**:
- `onCreate`: Called once, sets up Compose
- `onStart`/`onResume`: Composables become active
- `onPause`: `LaunchedEffect` pauses automatically
- Configuration changes: ViewModel persists

---

### Testing Strategy for Compose UI

**Key Findings**:
1. **Compose UI Test**: Use `createComposeRule()` for UI testing
2. **Semantics**: Add semantic properties for testing
3. **Manual Testing**: Required for swipe gestures (hard to automate)

**UI Test Pattern**:
```kotlin
@RunWith(AndroidJUnit4::class)
class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameScreen_renders_without_crashing() {
        composeTestRule.setContent {
            SnakeGameTheme {
                GameScreen()
            }
        }

        // Verify grid is displayed
        composeTestRule.onNodeWithTag("snake_grid").assertExists()
    }

    @Test
    fun snakeHead_is_displayed_at_initial_position() {
        composeTestRule.setContent {
            SnakeGameTheme {
                GameScreen()
            }
        }

        // Test would verify snake head rendering
        // Implementation depends on semantic tags
    }
}
```

**Manual Testing Plan**:
1. Deploy to emulator/device
2. Verify grid renders correctly
3. Test swipe gestures in all 4 directions
4. Verify reverse direction prevention works
5. Test on different screen sizes

---

## Integration Points

### Existing Code Integration

**What Already Exists**:
- ✅ GameViewModel with `handleDirectionInput()`
- ✅ GameState with `snake`, `score`, `isGameOver`
- ✅ Direction enum (UP, DOWN, LEFT, RIGHT)
- ✅ Snake model with `head`, `body`, `direction`
- ✅ ValidateDirectionUseCase for direction validation

**What Needs to be Added**:
- ❌ MainActivity.kt
- ❌ GameScreen.kt (composable)
- ❌ SnakeGrid.kt (Canvas-based rendering)
- ❌ Color.kt and Theme.kt
- ❌ AndroidManifest.xml update

**Integration Flow**:
```
MainActivity.onCreate()
  └─> setContent { SnakeGameTheme { GameScreen() } }
       └─> GameScreen creates/gets GameViewModel
            └─> Observes gameState via collectAsState()
            └─> Renders SnakeGrid with current state
            └─> Detects swipe gestures
                 └─> Calls viewModel.handleDirectionInput()
                      └─> ValidateDirectionUseCase validates
                           └─> Updates gameState if valid
                                └─> UI recomposes automatically
```

---

## Risk Assessment

### Low Risk
- ✅ Jetpack Compose is stable and well-documented
- ✅ Canvas rendering is proven for games
- ✅ ViewModel integration is straightforward
- ✅ Swipe gesture detection is simple

### Medium Risk
- ⚠️ **60 FPS Performance**: Need to profile Canvas rendering
  - *Mitigation*: Use `remember` to cache calculations, minimize recomposition
- ⚠️ **Swipe Sensitivity**: May need tuning for different devices
  - *Mitigation*: Use relative drag amounts, test on multiple devices

### Identified Risks Mitigated
- ❌ **Over-engineering**: Using simple Canvas instead of complex custom views
- ❌ **Third-party Dependencies**: Using native Compose APIs only

---

## Dependencies and Configuration

### Build Configuration (Already Configured)

From `app/build.gradle.kts`:
```kotlin
android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
```

**No Additional Dependencies Needed** - All required Compose libraries already configured.

---

## Open Questions (All Resolved)

### Q1: Should we use Canvas or LazyGrid for game rendering?
**Answer**: Canvas (see Decision #2)

### Q2: How to handle swipe gestures in Compose?
**Answer**: `Modifier.pointerInput` with `detectDragGestures` (see Decision #3)

### Q3: How to integrate with existing ViewModel?
**Answer**: Use `viewModel()` composable and `collectAsState()` (see Decision #1)

### Q4: What theme system for Compose?
**Answer**: Material3 with custom game colors (see Decision #4)

### Q5: How to support different screen sizes?
**Answer**: `BoxWithConstraints` for responsive sizing (see Decision #5)

---

## Next Steps (Phase 1)

1. ✅ **Research complete**: All technical unknowns resolved
2. → **Create MainActivity.kt**: Entry point with setContent
3. → **Create Color.kt and Theme.kt**: Game color scheme and Material3 theme
4. → **Create GameScreen.kt**: Main Compose screen with swipe gestures
5. → **Create SnakeGrid.kt**: Canvas-based grid rendering
6. → **Update quickstart.md**: Build and run instructions
7. → **Update AndroidManifest.xml**: Ensure MainActivity is launcher activity

---

**Research Status**: ✅ **COMPLETE** - All design decisions finalized, ready for Phase 1 implementation.
