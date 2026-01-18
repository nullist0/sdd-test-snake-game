# Snake Game - Android

A classic Snake game implementation for Android using Kotlin and Jetpack Compose.

## Features Implemented

### 009: No Reverse Direction Control ✅
Prevents reverse (180°) direction changes to avoid instant self-collision.

- **Reverse Prevention**: Blocks UP↔DOWN and LEFT↔RIGHT direction changes
- **Perpendicular Control**: Allows 90° turns in all valid directions
- **Same Direction Handling**: Gracefully handles repeated same-direction inputs

## Technical Stack

- **Language**: Kotlin 1.9+
- **Platform**: Android SDK 24+ (Android 7.0 Nougat)
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture (UI → Domain → Data)
- **State Management**: StateFlow
- **Testing**: JUnit 4, Kotlin Test

## Project Structure

```
app/
├── src/
│   ├── main/java/com/snakegame/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Direction.kt          # Direction enum with validation
│   │   │   │   ├── Position.kt           # Grid position
│   │   │   │   └── Snake.kt              # Snake entity
│   │   │   └── usecase/
│   │   │       └── ValidateDirectionUseCase.kt  # Direction validation logic
│   │   └── ui/
│   │       └── game/
│   │           ├── GameState.kt          # Game state data class
│   │           └── GameViewModel.kt      # Game state management
│   └── test/java/com/snakegame/
│       ├── domain/
│       │   ├── model/
│       │   │   └── DirectionTest.kt      # Direction enum tests
│       │   └── usecase/
│       │       └── ValidateDirectionUseCaseTest.kt  # Validation tests
│       └── ui/
│           └── game/
│               └── GameViewModelTest.kt  # ViewModel integration tests
```

## Building the Project

This project requires:
- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or later
- Android SDK 24+

### Setup

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run tests: `./gradlew test`
5. Build APK: `./gradlew assembleDebug`

## Running Tests

The project follows Test-Driven Development (TDD):

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests DirectionTest

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

### Test Coverage

- **Direction enum**: 100% coverage (all methods tested)
- **ValidateDirectionUseCase**: 100% coverage (16 direction combinations)
- **GameViewModel**: Integration tests for all direction validation scenarios

## Development

### Direction Validation Logic

The core validation prevents reverse direction changes:

```kotlin
// Reverse direction pairs (rejected)
UP ↔ DOWN
LEFT ↔ RIGHT

// Perpendicular directions (accepted)
UP → LEFT/RIGHT
DOWN → LEFT/RIGHT
LEFT → UP/DOWN
RIGHT → UP/DOWN

// Same direction (accepted)
UP → UP, DOWN → DOWN, LEFT → LEFT, RIGHT → RIGHT
```

### Adding New Features

Follow the TDD approach:
1. Write failing tests first (RED)
2. Implement minimum code to pass (GREEN)
3. Refactor for quality (REFACTOR)

## Specifications

Detailed specifications available in `specs/` directory:
- `009-no-reverse-direction/spec.md` - Feature specification
- `009-no-reverse-direction/plan.md` - Implementation plan
- `009-no-reverse-direction/tasks.md` - Task breakdown
- `009-no-reverse-direction/data-model.md` - Data models
- `009-no-reverse-direction/contracts/` - API contracts

## License

This is a demo project for educational purposes.

## Status

✅ Phase 1: Setup Complete
✅ Phase 2: Foundational Models Complete
✅ Phase 3: User Story 1 (MVP) - Reverse Prevention Complete
✅ Phase 4: User Story 2 - Perpendicular Control Complete
✅ Phase 5: User Story 3 - Same Direction Handling Complete

**Next Steps**:
- Implement swipe gesture detection (Feature 001)
- Add snake movement and game loop
- Implement collision detection (Feature 005)
- Add fruit spawning and collection
- Create UI with Jetpack Compose
