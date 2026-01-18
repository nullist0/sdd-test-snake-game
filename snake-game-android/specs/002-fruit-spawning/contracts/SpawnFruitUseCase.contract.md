# Contract: SpawnFruitUseCase

**Component Type**: Use Case (Business Logic)
**Package**: `com.snakegame.domain.usecase`
**Responsibility**: Orchestrate fruit spawning with 3x3 tail preference and grid fallback

## Interface

```kotlin
class SpawnFruitUseCase(
    private val calculateSpawnZone: CalculateSpawnZoneUseCase,
    private val findEmptyCells: FindEmptyCellsUseCase
) {
    operator fun invoke(gameState: GameState): Position
}
```

## Behavior

**Tier 1 (Preferred)**:
1. Calculate 3x3 zone centered on snake tail
2. Find empty cells in zone
3. If non-empty: return random cell from zone

**Tier 2 (Fallback)**:
4. Generate all grid positions
5. Find empty cells in entire grid
6. If non-empty: return random cell from grid
7. If empty: throw IllegalStateException("No empty cells - game over")

## Preconditions

- GameState has valid snake with accessible tail position
- Grid dimensions > 0

## Postconditions

**Success**: Returns Position within grid bounds, not occupied by snake
**Failure**: Throws exception if no empty cells exist

## Performance

- Target: <50ms for 30x30 grid
- Typical: <10ms for 10x10 grid

**Version**: 1.0 | **Date**: 2026-01-18
