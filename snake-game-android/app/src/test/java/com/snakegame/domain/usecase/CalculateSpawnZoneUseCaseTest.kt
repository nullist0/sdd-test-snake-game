package com.snakegame.domain.usecase

import com.snakegame.domain.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for CalculateSpawnZoneUseCase.
 *
 * Tests the 3x3 tail-centered spawn zone calculation with boundary clamping.
 * Following TDD: RED phase - tests written before implementation.
 */
class CalculateSpawnZoneUseCaseTest {

    @Test
    fun `center position returns 9 cells in 3x3 grid`() {
        // Given: tail at center of 10x10 grid
        val useCase = CalculateSpawnZoneUseCase()
        val tailPosition = Position(5, 5)
        val gridWidth = 10
        val gridHeight = 10

        // When: calculate spawn zone
        val zone = useCase(tailPosition, gridWidth, gridHeight)

        // Then: should return 9 cells (3x3 area)
        assertEquals(9, zone.size)

        // Verify all 9 expected positions present
        val expectedPositions = listOf(
            Position(4, 4), Position(5, 4), Position(6, 4),
            Position(4, 5), Position(5, 5), Position(6, 5),
            Position(4, 6), Position(5, 6), Position(6, 6)
        )
        assertTrue(zone.containsAll(expectedPositions))
    }

    @Test
    fun `corner position (0,0) returns 4 cells (2x2, clipped to bounds)`() {
        // Given: tail at top-left corner
        val useCase = CalculateSpawnZoneUseCase()
        val tailPosition = Position(0, 0)
        val gridWidth = 10
        val gridHeight = 10

        // When: calculate spawn zone
        val zone = useCase(tailPosition, gridWidth, gridHeight)

        // Then: should return 4 cells (boundary clipped)
        assertEquals(4, zone.size)

        val expectedPositions = listOf(
            Position(0, 0), Position(1, 0),
            Position(0, 1), Position(1, 1)
        )
        assertTrue(zone.containsAll(expectedPositions))
    }

    @Test
    fun `edge position (0,5) returns 6 cells (2x3, clipped)`() {
        // Given: tail at left edge (not corner)
        val useCase = CalculateSpawnZoneUseCase()
        val tailPosition = Position(0, 5)
        val gridWidth = 10
        val gridHeight = 10

        // When: calculate spawn zone
        val zone = useCase(tailPosition, gridWidth, gridHeight)

        // Then: should return 6 cells (clipped on left side)
        assertEquals(6, zone.size)

        val expectedPositions = listOf(
            Position(0, 4), Position(1, 4),
            Position(0, 5), Position(1, 5),
            Position(0, 6), Position(1, 6)
        )
        assertTrue(zone.containsAll(expectedPositions))
    }

    @Test
    fun `single-cell grid (1x1) returns 1 cell`() {
        // Given: minimal 1x1 grid
        val useCase = CalculateSpawnZoneUseCase()
        val tailPosition = Position(0, 0)
        val gridWidth = 1
        val gridHeight = 1

        // When: calculate spawn zone
        val zone = useCase(tailPosition, gridWidth, gridHeight)

        // Then: should return just that cell
        assertEquals(1, zone.size)
        assertTrue(zone.contains(Position(0, 0)))
    }

    @Test
    fun `all returned positions are within grid bounds`() {
        // Given: tail near bottom-right corner
        val useCase = CalculateSpawnZoneUseCase()
        val tailPosition = Position(8, 8)
        val gridWidth = 10
        val gridHeight = 10

        // When: calculate spawn zone
        val zone = useCase(tailPosition, gridWidth, gridHeight)

        // Then: all positions should be within bounds
        assertTrue(zone.all { it.x >= 0 && it.x < gridWidth })
        assertTrue(zone.all { it.y >= 0 && it.y < gridHeight })
    }

    @Test
    fun `positions include tail position and 8 adjacent cells for center`() {
        // Given: tail with full 3x3 surrounding space
        val useCase = CalculateSpawnZoneUseCase()
        val tailPosition = Position(5, 5)
        val gridWidth = 15
        val gridHeight = 15

        // When: calculate spawn zone
        val zone = useCase(tailPosition, gridWidth, gridHeight)

        // Then: should include tail and all 8 adjacent cells
        assertEquals(9, zone.size)
        assertTrue(zone.contains(tailPosition)) // tail itself
        assertTrue(zone.contains(Position(4, 4))) // top-left
        assertTrue(zone.contains(Position(5, 4))) // top
        assertTrue(zone.contains(Position(6, 4))) // top-right
        assertTrue(zone.contains(Position(4, 5))) // left
        assertTrue(zone.contains(Position(6, 5))) // right
        assertTrue(zone.contains(Position(4, 6))) // bottom-left
        assertTrue(zone.contains(Position(5, 6))) // bottom
        assertTrue(zone.contains(Position(6, 6))) // bottom-right
    }
}
