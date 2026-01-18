# Research: Strategic Fruit Spawning

**Feature**: 002-fruit-spawning
**Date**: 2026-01-18
**Status**: Complete

## Overview

This document contains research findings for implementing strategic fruit spawning in the snake game with a 3x3 tail-centered preference zone and fallback to random grid placement. Research focused on spawn algorithms, grid boundary handling, random selection approaches, and rendering strategies for Compose Canvas.

## Research Questions Resolved

### 1. Spawn Zone Calculation Algorithm

**Decision**: Use simple offset-based 3x3 grid calculation with boundary clamping

**Algorithm**:
```kotlin
fun calculateSpawnZone(tailPosition: Position, gridWidth: Int, gridHeight: Int): List<Position> {
    val zone = mutableListOf<Position>()

    // Calculate 3x3 area: tail ± 1 in both X and Y
    for (dx in -1..1) {
        for (dy in -1..1) {
            val x = tailPosition.x + dx
            val y = tailPosition.y + dy

            // Skip out-of-bounds positions
            if (x in 0 until gridWidth && y in 0 until gridHeight) {
                zone.add(Position(x, y))
            }
        }
    }

    return zone
}
```

**Rationale**:
- **Simple**: Direct coordinate math, no complex geometry
- **Boundary-safe**: Automatically clips positions outside grid bounds
- **Inclusive**: Includes tail position itself (will be filtered later as occupied)
- **Deterministic**: Same tail position always produces same zone
- **Efficient**: O(9) operations worst case, O(4) best case (corner positions)

**Edge Cases Handled**:
- **Corner positions** (0,0): Zone reduces to 4 cells (2x2)
- **Edge positions** (0, mid): Zone reduces to 6 cells (2x3)
- **Center positions**: Full 9-cell zone
- **Single-cell grid** (1x1): Zone is just that cell

**Alternatives Considered**:
- **Pre-computed zone maps**: Rejected - memory overhead not justified for simple calculation
- **Diamond pattern (Manhattan distance)**: Rejected - spec explicitly requires 3x3 grid (includes diagonals)
- **Expandable zones**: Rejected - spec requires fixed 3x3, fallback is entire grid

### 2. Empty Cell Detection

**Decision**: Filter spawn zone by checking against snake body segments and existing fruit position

**Implementation**:
```kotlin
fun findEmptyCells(
    candidatePositions: List<Position>,
    snakeSegments: List<Position>,
    existingFruit: Position?
): List<Position> {
    val occupiedPositions = snakeSegments.toSet() + listOfNotNull(existingFruit)
    return candidatePositions.filter { it !in occupiedPositions }
}
```

**Rationale**:
- **Set-based lookup**: O(1) containment check for each candidate position
- **Immutable filtering**: Functional approach, no mutation
- **Null-safe**: Handles no existing fruit (game start) via `listOfNotNull`
- **Comprehensive**: Checks both snake body and fruit occupancy

**Performance**:
- Creating set from snake segments: O(n) where n = snake length
- Filtering candidates: O(m) where m = candidate count (max 9 for 3x3)
- **Total**: O(n + m), acceptable for snake game scales (n < 1000, m ≤ 9)

**Alternatives Considered**:
- **Grid array tracking**: Rejected - memory overhead for large grids, state synchronization complexity
- **Quadtree spatial indexing**: Rejected - overkill for snake game scales
- **List-based contains**: Rejected - O(n*m) vs O(n+m) for set approach

### 3. Random Cell Selection

**Decision**: Use Kotlin's built-in `random()` extension with uniform distribution

**Implementation**:
```kotlin
fun selectRandomCell(emptyCells: List<Position>): Position {
    require(emptyCells.isNotEmpty()) { "Cannot select from empty list" }
    return emptyCells.random()
}
```

**Rationale**:
- **Built-in**: Uses Kotlin stdlib `List.random()`, well-tested
- **Uniform distribution**: Each empty cell has equal probability (satisfies SC-006)
- **Simple**: Single line, no custom RNG logic
- **Thread-safe**: Uses ThreadLocalRandom internally (safe for Android)
- **Fail-fast**: `require` check catches programming errors early

**For Testing**:
```kotlin
// Inject Random instance for deterministic testing
fun selectRandomCell(emptyCells: List<Position>, random: Random = Random.Default): Position {
    require(emptyCells.isNotEmpty()) { "Cannot select from empty list" }
    return emptyCells.random(random)
}
```

**Alternatives Considered**:
- **Weighted random**: Rejected - spec requires uniform distribution
- **Pseudorandom with seed**: Kept as testing option via dependency injection
- **Deterministic selection** (e.g., first cell): Rejected - spec requires randomness

### 4. Fallback Strategy

**Decision**: Two-tier approach with clear separation between preferred and fallback

**Implementation**:
```kotlin
fun spawnFruit(gridState: GridState): Position {
    // Tier 1: Try 3x3 tail-centered zone
    val spawnZone = calculateSpawnZone(gridState.snakeTail, gridState.width, gridState.height)
    val emptyCellsInZone = findEmptyCells(spawnZone, gridState.snakeSegments, gridState.fruit?.position)

    if (emptyCellsInZone.isNotEmpty()) {
        return selectRandomCell(emptyCellsInZone)
    }

    // Tier 2: Fallback to entire grid
    val allGridPositions = (0 until gridState.width).flatMap { x ->
        (0 until gridState.height).map { y -> Position(x, y) }
    }
    val emptyCellsInGrid = findEmptyCells(allGridPositions, gridState.snakeSegments, gridState.fruit?.position)

    require(emptyCellsInGrid.isNotEmpty()) { "No empty cells on grid - game over" }
    return selectRandomCell(emptyCellsInGrid)
}
```

**Rationale**:
- **Clear tier separation**: Preferred → Fallback, no ambiguity
- **Early return**: Exit as soon as valid position found (efficiency)
- **Fail-fast**: Game over detection when grid full
- **Satisfies requirements**: FR-001 (preferred 3x3), FR-004 (fallback random)

**Performance**:
- **Best case** (empty zone): O(9 + n) where n = snake length
- **Worst case** (fallback): O(w*h + n) where w,h = grid dimensions
- **Typical** (10x10 grid, snake length 20): ~120 operations, well under 50ms target

**Alternatives Considered**:
- **Unified approach**: Rejected - obscures two-tier logic, harder to test
- **Expanding zones**: Rejected - spec requires 3x3 then entire grid, no intermediate
- **Cache empty cells**: Considered - may add if profiling shows performance issues

### 5. Grid State Management

**Decision**: Extend existing GameState with Fruit property

**Data Model**:
```kotlin
data class Fruit(
    val position: Position,
    val isActive: Boolean = true
)

data class GameState(
    val snake: Snake,
    val fruit: Fruit?,  // Null = no fruit (game start or mid-collection)
    // ... other game state
) {
    companion object {
        fun initial(gridWidth: Int, gridHeight: Int): GameState {
            val initialSnake = Snake.initial()
            val initialFruit = spawnInitialFruit(initialSnake, gridWidth, gridHeight)
            return GameState(snake = initialSnake, fruit = initialFruit)
        }
    }
}
```

**Rationale**:
- **Single source of truth**: Fruit position lives in GameState
- **Nullable**: Supports no-fruit state (transitional)
- **Immutable**: Fruit is data class, updates create new instances
- **StateFlow integration**: Works with existing ViewModel pattern

**State Transitions**:
```
[Game Start] → fruit = null
    ↓
[Initial Spawn] → fruit = Fruit(pos, active=true)
    ↓
[Snake eats fruit] → fruit = null (transitional)
    ↓
[New fruit spawns] → fruit = Fruit(newPos, active=true)
    ↓
[Repeat collection cycle]
```

**Alternatives Considered**:
- **Separate FruitManager**: Rejected - adds complexity, fruit is part of game state
- **Multiple fruits**: Rejected - spec limits to single fruit
- **Fruit pool/cache**: Rejected - single fruit, no need for pooling

### 6. Rendering Approach (Compose Canvas)

**Decision**: Use Compose Canvas with simple shape drawing

**Implementation**:
```kotlin
@Composable
fun FruitRenderer(
    fruit: Fruit?,
    cellSize: Dp,
    modifier: Modifier = Modifier
) {
    if (fruit != null && fruit.isActive) {
        Canvas(modifier = modifier) {
            val fruitX = fruit.position.x * cellSize.toPx()
            val fruitY = fruit.position.y * cellSize.toPx()
            val radius = cellSize.toPx() * 0.4f  // 40% of cell size

            drawCircle(
                color = Color.Red,
                radius = radius,
                center = Offset(fruitX + cellSize.toPx() / 2, fruitY + cellSize.toPx() / 2)
            )
        }
    }
}
```

**Rationale**:
- **Simple**: Circle shape, easy to distinguish from square snake segments
- **Performant**: Direct Canvas API, no bitmap overhead
- **Scalable**: Works across all screen sizes/densities
- **Compose-native**: Integrates with existing Compose setup from feature 001

**Visual Design**:
- **Color**: Red (high contrast with green snake, white background)
- **Size**: 40% of cell size (fits comfortably within grid cell)
- **Shape**: Circle (distinct from rectangular snake segments)
- **Position**: Centered within grid cell

**Alternatives Considered**:
- **Image/Icon**: Rejected - adds asset management, not needed for MVP
- **Animated spawn**: Rejected - out of scope per spec
- **Different shapes per fruit type**: Rejected - single fruit type in this feature

### 7. Spawn Timing and Triggers

**Decision**: Event-driven spawn on fruit collection and game initialization

**Implementation in GameViewModel**:
```kotlin
fun collectFruit(snakeHeadPosition: Position) {
    if (gameState.value.fruit?.position == snakeHeadPosition) {
        // 1. Clear current fruit
        _gameState.update { it.copy(fruit = null) }

        // 2. Grow snake
        growSnake()

        // 3. Spawn new fruit
        val newFruitPosition = spawnFruitUseCase(gameState.value)
        _gameState.update { it.copy(fruit = Fruit(newFruitPosition)) }
    }
}
```

**Rationale**:
- **Immediate**: <50ms from collection to new spawn (satisfies SC-003)
- **Deterministic**: Same grid state → same spawn position (for testing)
- **State-driven**: Fruit spawning reads current GameState
- **Event-based**: No polling, spawn happens on collection event

**Spawn Triggers**:
1. **Game initialization**: First fruit when game starts
2. **Fruit collection**: New fruit after snake eats previous one

**Alternatives Considered**:
- **Time-based spawning**: Rejected - spec requires collection-triggered spawn
- **Delayed spawn**: Rejected - spec requires immediate spawn
- **Pre-spawning**: Rejected - spec requires single fruit at a time

## Testing Strategy

### Unit Tests

**CalculateSpawnZoneUseCaseTest**:
- Test center position returns 9 cells
- Test corner position returns 4 cells
- Test edge position returns 6 cells
- Test single-cell grid returns 1 cell
- Test all positions stay within grid bounds

**FindEmptyCellsUseCaseTest**:
- Test empty grid returns all candidates
- Test snake-occupied cells filtered out
- Test fruit-occupied cell filtered out
- Test mixed occupancy (some empty, some occupied)
- Test all-occupied returns empty list

**SpawnFruitUseCaseTest**:
- Test preferred zone used when empty cells available
- Test fallback to grid when preferred zone full
- Test exception when no empty cells anywhere
- Test deterministic spawn with fixed Random seed
- Test uniform distribution over many spawns (statistical test)

### Integration Tests

**GameViewModelTest** (expand existing):
- Test initial fruit spawns on game start
- Test fruit respawns after collection
- Test fruit spawns in 3x3 tail zone (P1)
- Test fruit uses fallback when tail zone blocked (P2)
- Test visual fruit appearance in state (P3)

### Performance Validation

**Benchmarks**:
- Measure spawn time for 10x10 grid: target <10ms
- Measure spawn time for 30x30 grid: target <50ms
- Measure 1000 spawns: verify <50ms p99 latency
- Profile memory allocations during spawn

## Summary

All research questions resolved:

| Question | Decision | Confidence |
|----------|----------|------------|
| Spawn zone calculation? | Offset-based 3x3 with boundary clamping | High - simple, handles all edge cases |
| Empty cell detection? | Set-based filtering | High - efficient O(n+m) |
| Random selection? | Kotlin stdlib `List.random()` | High - uniform distribution, well-tested |
| Fallback strategy? | Two-tier: preferred → entire grid | High - clear separation, satisfies requirements |
| State management? | Extend GameState with Fruit? property | High - single source of truth |
| Rendering approach? | Compose Canvas with circle shape | High - simple, performant, Compose-native |
| Spawn timing? | Event-driven on collection | High - immediate, deterministic |

**Status**: Research complete. Ready for Phase 1 (design artifacts).
