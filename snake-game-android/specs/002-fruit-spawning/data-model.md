# Data Model: Strategic Fruit Spawning

**Feature**: 002-fruit-spawning
**Date**: 2026-01-18

## Domain Models

### Fruit

**Purpose**: Represents a collectible game object that spawns on the grid

**Location**: `app/src/main/java/com/snakegame/domain/model/Fruit.kt`

**Definition**:
```kotlin
data class Fruit(
    val position: Position,
    val isActive: Boolean = true
)
```

**Fields**:
- `position`: Grid coordinates where fruit is located
- `isActive`: Whether fruit can be collected (always true for this feature)

**Validation Rules**:
- Position must be within grid bounds
- Position must not overlap with snake segments
- Only one active fruit at a time

**State Transitions**:
```
[No Fruit] → spawn() → [Active Fruit at position]
[Active Fruit] → collect() → [No Fruit] → spawn() → [Active Fruit at new position]
```

### GameState (Modified)

**Purpose**: Extended to include fruit

**Definition**:
```kotlin
data class GameState(
    val snake: Snake,
    val fruit: Fruit?,  // New field
    // ... existing fields
)
```

**Changes from Feature 001**:
- Added `fruit: Fruit?` field (nullable to support no-fruit state)
- Added spawn logic to initial() factory method

## Use Case Models

### SpawnZone

**Purpose**: Value object representing the 3x3 tail-centered preferred spawn area

**Definition**:
```kotlin
data class SpawnZone(
    val centerPosition: Position,
    val candidatePositions: List<Position>
)
```

**Computed from**:
- Snake tail position
- Grid boundaries (width, height)

### GridState

**Purpose**: Aggregates all grid information needed for spawn calculation

**Definition**:
```kotlin
data class GridState(
    val width: Int,
    val height: Int,
    val snakeTail: Position,
    val snakeSegments: List<Position>,
    val fruit: Fruit?
)
```

**Derived from GameState**: Created on-demand for spawn logic

## Entity Relationships

```
GameState
    ├── snake: Snake
    │     └── tail: Position (used for spawn zone center)
    └── fruit: Fruit? (spawned result)
              └── position: Position

SpawnZone (transient, computed)
    └── candidatePositions: List<Position>

GridState (transient, aggregator)
    ├── snakeTail: Position
    ├── snakeSegments: List<Position>
    └── fruit: Fruit?
```

## Validation Rules Summary

| Entity | Rule | Enforced By |
|--------|------|-------------|
| Fruit.position | Within grid bounds | SpawnFruitUseCase |
| Fruit.position | Not on snake segment | FindEmptyCellsUseCase |
| GameState.fruit | At most one active | Game logic (collection clears, spawn creates) |
| SpawnZone | All positions within grid | CalculateSpawnZoneUseCase |

## Testing Implications

**Unit Tests**:
- Fruit: immutability, equality
- SpawnZone: boundary handling, position generation
- GridState: aggregation correctness

**Integration Tests**:
- GameState with Fruit: spawn → collect → respawn cycle
- Fruit rendering: position → screen coordinates

**Status**: Data model design complete
