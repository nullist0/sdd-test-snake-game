# Quickstart: Strategic Fruit Spawning Implementation

**Feature**: 002-fruit-spawning
**Estimated Time**: 2-3 hours (TDD approach)

## Implementation Roadmap

### Phase 1: Domain Models (30 min)
1. Create `Fruit` data class
2. Create `SpawnZone` value object
3. Extend `GameState` with `fruit: Fruit?`

### Phase 2: Spawn Logic (60 min)
4. Create `CalculateSpawnZoneUseCase` + tests
5. Create `FindEmptyCellsUseCase` + tests
6. Create `SpawnFruitUseCase` + tests (orchestrator)

### Phase 3: Integration (45 min)
7. Modify `GameViewModel` to handle fruit spawn
8. Add fruit collection logic
9. Integration tests

### Phase 4: Rendering (30 min)
10. Create `FruitRenderer` composable
11. Integrate with `GameScreen`
12. Visual tests

## Key Implementation Points

**Spawn Algorithm** (research.md):
```kotlin
// 1. Try 3x3 tail zone
val zone = calculateSpawnZone(snake.tail, gridWidth, gridHeight)
val empty = findEmptyCells(zone, snakeSegments, existingFruit)

if (empty.isNotEmpty()) return empty.random()

// 2. Fallback to entire grid
val allPositions = /* all grid cells */
val emptyGrid = findEmptyCells(allPositions, snakeSegments, existingFruit)

return emptyGrid.random()
```

**TDD Workflow**:
- Write tests FIRST (Red)
- Implement to pass (Green)
- Refactor for quality

**Testing Focus**:
- Boundary cases (corners, edges)
- Fallback logic (3x3 → grid)
- Uniform random distribution
- Performance (<50ms)

## Success Criteria

✅ Fruit spawns in 3x3 tail area 95% of time when available
✅ Fallback works when tail area blocked
✅ Visual fruit immediately visible
✅ Performance <50ms spawn time

**Status**: Ready for `/speckit.tasks` command
