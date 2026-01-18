package com.snakegame.domain.usecase

import com.snakegame.domain.model.Fruit
import com.snakegame.domain.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for FindEmptyCellsUseCase.
 *
 * Tests filtering of candidate positions to find cells not occupied by snake or fruit.
 * Following TDD: RED phase - tests written before implementation.
 */
class FindEmptyCellsUseCaseTest {

    @Test
    fun `empty grid returns all candidate positions`() {
        // Given: no snake, no fruit, 9 candidate positions
        val useCase = FindEmptyCellsUseCase()
        val candidates = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4),
            Position(4, 5), Position(5, 5), Position(6, 5),
            Position(4, 6), Position(5, 6), Position(6, 6)
        )
        val snakeSegments = emptyList<Position>()
        val existingFruit: Fruit? = null

        // When: find empty cells
        val emptyCells = useCase(candidates, snakeSegments, existingFruit)

        // Then: should return all candidates
        assertEquals(9, emptyCells.size)
        assertTrue(emptyCells.containsAll(candidates))
    }

    @Test
    fun `snake-occupied cells are filtered out`() {
        // Given: snake occupies 3 positions in the zone
        val useCase = FindEmptyCellsUseCase()
        val candidates = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4),
            Position(4, 5), Position(5, 5), Position(6, 5)
        )
        val snakeSegments = listOf(
            Position(5, 4), // head
            Position(5, 5), // body
            Position(5, 6)  // tail (not in candidates, but should be in occupied set)
        )
        val existingFruit: Fruit? = null

        // When: find empty cells
        val emptyCells = useCase(candidates, snakeSegments, existingFruit)

        // Then: should exclude snake positions
        assertEquals(4, emptyCells.size)
        assertTrue(emptyCells.contains(Position(4, 4)))
        assertTrue(emptyCells.contains(Position(6, 4)))
        assertTrue(emptyCells.contains(Position(4, 5)))
        assertTrue(emptyCells.contains(Position(6, 5)))
    }

    @Test
    fun `fruit-occupied cell is filtered out`() {
        // Given: fruit occupies one position
        val useCase = FindEmptyCellsUseCase()
        val candidates = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4)
        )
        val snakeSegments = emptyList<Position>()
        val existingFruit = Fruit(position = Position(5, 4))

        // When: find empty cells
        val emptyCells = useCase(candidates, snakeSegments, existingFruit)

        // Then: should exclude fruit position
        assertEquals(2, emptyCells.size)
        assertTrue(emptyCells.contains(Position(4, 4)))
        assertTrue(emptyCells.contains(Position(6, 4)))
    }

    @Test
    fun `mixed occupancy - some empty, some occupied`() {
        // Given: snake and fruit occupy some positions
        val useCase = FindEmptyCellsUseCase()
        val candidates = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4),
            Position(4, 5), Position(5, 5), Position(6, 5),
            Position(4, 6), Position(5, 6), Position(6, 6)
        )
        val snakeSegments = listOf(
            Position(5, 5), // center
            Position(4, 5), // left
            Position(6, 5)  // right
        )
        val existingFruit = Fruit(position = Position(5, 4)) // top center

        // When: find empty cells
        val emptyCells = useCase(candidates, snakeSegments, existingFruit)

        // Then: should return only unoccupied cells
        assertEquals(5, emptyCells.size)
        val expectedEmpty = listOf(
            Position(4, 4), Position(6, 4), // top row (excluding fruit)
            Position(4, 6), Position(5, 6), Position(6, 6) // bottom row
        )
        assertTrue(emptyCells.containsAll(expectedEmpty))
    }

    @Test
    fun `all-occupied candidates returns empty list`() {
        // Given: all candidates occupied
        val useCase = FindEmptyCellsUseCase()
        val candidates = listOf(
            Position(4, 4), Position(5, 4),
            Position(4, 5), Position(5, 5)
        )
        val snakeSegments = listOf(
            Position(4, 4), Position(5, 4), Position(4, 5)
        )
        val existingFruit = Fruit(position = Position(5, 5))

        // When: find empty cells
        val emptyCells = useCase(candidates, snakeSegments, existingFruit)

        // Then: should return empty list
        assertTrue(emptyCells.isEmpty())
    }

    @Test
    fun `no fruit (null) only filters snake segments`() {
        // Given: null fruit (game start scenario)
        val useCase = FindEmptyCellsUseCase()
        val candidates = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4)
        )
        val snakeSegments = listOf(Position(5, 4))
        val existingFruit: Fruit? = null

        // When: find empty cells
        val emptyCells = useCase(candidates, snakeSegments, existingFruit)

        // Then: should only filter snake, not crash on null fruit
        assertEquals(2, emptyCells.size)
        assertTrue(emptyCells.contains(Position(4, 4)))
        assertTrue(emptyCells.contains(Position(6, 4)))
    }
}
