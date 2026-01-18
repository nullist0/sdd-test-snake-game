package com.snakegame.domain.usecase

import com.snakegame.domain.model.Fruit
import com.snakegame.domain.model.Position
import com.snakegame.domain.model.Snake
import com.snakegame.ui.game.GameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Unit tests for SpawnFruitUseCase.
 *
 * Tests preferred zone spawning (US1) and fallback logic (US2).
 * Following TDD: RED phase - tests written before implementation.
 */
class SpawnFruitUseCaseTest {

    // ========== USER STORY 1: Preferred Zone Tests ==========

    @Test
    fun `spawns in 3x3 zone when empty cells available`() {
        // Given: snake with tail (last body segment) at (5,5) with empty surrounding cells
        val snake = Snake(
            head = Position(5, 3),
            body = listOf(Position(5, 4), Position(5, 5)),
            direction = Direction.UP
        )
        val gameState = GameState(snake = snake, fruit = null)
        val useCase = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10
        )

        // When: spawn fruit
        val fruitPosition = useCase(gameState)

        // Then: should spawn in 3x3 zone around tail at (5,5)
        val expectedZone = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4),
            Position(4, 5), Position(5, 5), Position(6, 5),
            Position(4, 6), Position(5, 6), Position(6, 6)
        )
        assertTrue("Fruit should spawn in 3x3 tail zone", fruitPosition in expectedZone)

        // Should not spawn on snake
        val snakeSegments = listOf(snake.head) + snake.body
        assertTrue("Fruit should not spawn on snake", fruitPosition !in snakeSegments)
    }

    @Test
    fun `random selection from multiple empty cells in zone`() {
        // Given: small snake with tail at (5,3) - many empty cells in zone
        val snake = Snake(
            head = Position(5, 3),
            body = emptyList(),
            direction = Direction.UP
        )
        val gameState = GameState(snake = snake, fruit = null)
        val useCase = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10
        )

        // When: spawn fruit multiple times
        val spawns = (1..100).map { useCase(gameState) }.toSet()

        // Then: should produce variety (not always same position)
        assertTrue("Should use random selection, got ${spawns.size} unique positions", spawns.size > 1)
    }

    @Test
    fun `deterministic spawn with fixed Random seed`() {
        // Given: two identical game states and use cases with same seed
        val snake = Snake(
            head = Position(5, 3),
            body = emptyList(),
            direction = Direction.UP
        )
        val gameState = GameState(snake = snake, fruit = null)

        val seed = 42L
        val useCase1 = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10,
            random = Random(seed)
        )
        val useCase2 = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10,
            random = Random(seed)
        )

        // When: spawn with same seed
        val position1 = useCase1(gameState)
        val position2 = useCase2(gameState)

        // Then: should produce identical results
        assertEquals("Same seed should produce same position", position1, position2)
    }

    @Test
    fun `spawns in one of limited empty cells`() {
        // Given: tail zone with only 2 empty cells (rest occupied by snake)
        // Tail is last body segment at (5,6)
        val snake = Snake(
            head = Position(5, 5),
            body = listOf(
                Position(4, 4), Position(5, 4), Position(6, 4),
                Position(4, 5), // next to head
                Position(4, 6), Position(5, 6) // tail at (5,6)
            ),
            direction = Direction.DOWN
        )
        val gameState = GameState(snake = snake, fruit = null)
        val useCase = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10
        )

        // When: spawn fruit
        val fruitPosition = useCase(gameState)

        // Then: should spawn in one of the 2 empty cells
        val expectedPositions = listOf(Position(6, 5), Position(6, 6))
        assertTrue("Fruit should spawn in one of 2 empty cells", fruitPosition in expectedPositions)
    }

    // ========== USER STORY 2: Fallback Tests (added in Phase 4) ==========

    @Test
    fun `fallback to grid when 3x3 zone fully occupied`() {
        // Given: tail zone completely occupied, but grid has empty cells
        // Tail is last body segment at (5,6)
        val snake = Snake(
            head = Position(5, 5),
            body = listOf(
                Position(4, 4), Position(5, 4), Position(6, 4),
                Position(4, 5), Position(6, 5),
                Position(4, 6), Position(5, 6), Position(6, 6) // tail at (5,6)
            ),
            direction = Direction.DOWN
        )
        val gameState = GameState(snake = snake, fruit = null)
        val useCase = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10
        )

        // When: spawn fruit
        val fruitPosition = useCase(gameState)

        // Then: should spawn outside 3x3 zone (fallback used)
        val tailZone = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4),
            Position(4, 5), Position(5, 5), Position(6, 5),
            Position(4, 6), Position(5, 6), Position(6, 6)
        )
        assertTrue("Fruit should spawn outside blocked 3x3 zone", fruitPosition !in tailZone)

        // Should still be within grid bounds
        assertTrue("Fruit X should be within grid", fruitPosition.x in 0 until 10)
        assertTrue("Fruit Y should be within grid", fruitPosition.y in 0 until 10)

        // Should not spawn on snake
        val snakeSegments = listOf(snake.head) + snake.body
        assertTrue("Fruit should not spawn on snake", fruitPosition !in snakeSegments)
    }

    @Test
    fun `fallback when only tail zone blocked - many empty cells elsewhere`() {
        // Given: small snake blocking just the tail zone
        val snake = Snake(
            head = Position(1, 1),
            body = listOf(
                Position(0, 0), Position(1, 0), Position(2, 0),
                Position(0, 1), Position(2, 1),
                Position(0, 2), Position(1, 2), Position(2, 2) // tail at (2,2)
            ),
            direction = Direction.RIGHT
        )
        val gameState = GameState(snake = snake, fruit = null)
        val useCase = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10
        )

        // When: spawn fruit
        val fruitPosition = useCase(gameState)

        // Then: should use fallback (entire grid has many empty cells)
        assertTrue("Fruit should spawn successfully", fruitPosition.x in 0 until 10)
        assertTrue("Fruit should spawn successfully", fruitPosition.y in 0 until 10)

        val snakeSegments = listOf(snake.head) + snake.body
        assertTrue("Fruit should not spawn on snake", fruitPosition !in snakeSegments)
    }

    @Test
    fun `exception when no empty cells anywhere on grid`() {
        // Given: impossible scenario - entire grid occupied (game over state)
        // For 3x3 grid completely filled
        val snake = Snake(
            head = Position(1, 1),
            body = listOf(
                Position(0, 0), Position(1, 0), Position(2, 0),
                Position(0, 1), Position(2, 1),
                Position(0, 2), Position(1, 2), Position(2, 2)
            ),
            direction = Direction.RIGHT
        )
        val existingFruit = Fruit(position = Position(0, 0)) // doesn't matter, all occupied
        val gameState = GameState(snake = snake, fruit = existingFruit)
        val useCase = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 3,
            gridHeight = 3
        )

        // When/Then: should throw (no empty cells anywhere)
        assertThrows(IllegalStateException::class.java) {
            useCase(gameState)
        }
    }

    @Test
    fun `uniform distribution across grid in fallback mode`() {
        // Given: zone blocked, forcing fallback
        val snake = Snake(
            head = Position(5, 5),
            body = listOf(
                Position(4, 4), Position(5, 4), Position(6, 4),
                Position(4, 5), Position(6, 5),
                Position(4, 6), Position(5, 6), Position(6, 6)
            ),
            direction = Direction.DOWN
        )
        val gameState = GameState(snake = snake, fruit = null)
        val useCase = SpawnFruitUseCase(
            calculateSpawnZone = CalculateSpawnZoneUseCase(),
            findEmptyCells = FindEmptyCellsUseCase(),
            gridWidth = 10,
            gridHeight = 10
        )

        // When: spawn many times
        val spawns = (1..100).map { useCase(gameState) }

        // Then: should have variety (not all same position)
        val uniquePositions = spawns.toSet()
        assertTrue(
            "Fallback should produce variety, got ${uniquePositions.size} unique positions",
            uniquePositions.size > 1
        )

        // All should be outside the blocked zone
        val blockedZone = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4),
            Position(4, 5), Position(5, 5), Position(6, 5),
            Position(4, 6), Position(5, 6), Position(6, 6)
        )
        assertTrue("All spawns should avoid blocked zone", spawns.none { it in blockedZone })
    }
}
