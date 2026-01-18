package com.snakegame.ui.game

import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.Position
import com.snakegame.domain.usecase.CalculateSpawnZoneUseCase
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for GameViewModel direction validation integration.
 *
 * Tests that the ViewModel correctly uses ValidateDirectionUseCase
 * to accept valid directions and reject reverse directions.
 */
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

    // === User Story 2: Perpendicular Direction Control Tests ===

    @Test
    fun `accepts perpendicular turn from UP to LEFT`() {
        viewModel.handleDirectionInput(Direction.UP)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.LEFT)
        assertEquals(Direction.LEFT, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `accepts perpendicular turn from UP to RIGHT`() {
        viewModel.handleDirectionInput(Direction.UP)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.RIGHT)
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `accepts sequence of perpendicular turns forming square path`() {
        // RIGHT (initial) → UP → LEFT → DOWN → RIGHT
        viewModel.handleDirectionInput(Direction.UP)
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.LEFT)
        assertEquals(Direction.LEFT, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.DOWN)
        assertEquals(Direction.DOWN, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.RIGHT)
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
    }

    // === User Story 3: Same Direction Handling Tests ===

    @Test
    fun `accepts same direction input repeatedly`() {
        // Initial direction is RIGHT
        viewModel.handleDirectionInput(Direction.RIGHT)
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)

        // Input RIGHT again multiple times
        repeat(10) {
            viewModel.handleDirectionInput(Direction.RIGHT)
            assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
        }
    }

    @Test
    fun `handles alternating same and perpendicular directions`() {
        // RIGHT → RIGHT → UP → UP → LEFT → LEFT
        viewModel.handleDirectionInput(Direction.RIGHT)  // Same
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.UP)     // Perpendicular
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.UP)     // Same
        assertEquals(Direction.UP, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.LEFT)   // Perpendicular
        assertEquals(Direction.LEFT, viewModel.gameState.value.snake.direction)

        viewModel.handleDirectionInput(Direction.LEFT)   // Same
        assertEquals(Direction.LEFT, viewModel.gameState.value.snake.direction)
    }

    @Test
    fun `same direction does not cause errors or state corruption`() {
        val initialState = viewModel.gameState.value

        // Input same direction many times rapidly
        repeat(100) {
            viewModel.handleDirectionInput(Direction.RIGHT)
        }

        // State should be stable (direction unchanged, no corruption)
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
        assertEquals(initialState.snake.head, viewModel.gameState.value.snake.head)
        assertEquals(initialState.snake.body, viewModel.gameState.value.snake.body)
    }

    // === Feature 002: Fruit Spawning Tests ===

    @Test
    fun `initial fruit spawns on game start`() {
        // Given: ViewModel initialized
        // When: check initial game state
        val gameState = viewModel.gameState.value

        // Then: fruit should be present
        assertNotNull("Initial fruit should spawn on game start", gameState.fruit)
    }

    @Test
    fun `fruit spawns in 3x3 tail area`() {
        // Given: ViewModel with initial state
        val gameState = viewModel.gameState.value
        val fruit = gameState.fruit

        // Then: fruit should be spawned
        assertNotNull("Fruit should be present", fruit)

        // Get tail position (last body segment or head)
        val tailPosition = gameState.snake.body.lastOrNull() ?: gameState.snake.head

        // Calculate expected 3x3 zone
        val calculateZone = CalculateSpawnZoneUseCase()
        val expectedZone = calculateZone(
            tailPosition,
            gridWidth = 15, // default grid size from GameState.initial()
            gridHeight = 15
        )

        // Fruit should be within 3x3 tail zone
        assertTrue(
            "Fruit should spawn in 3x3 tail zone, but was at ${fruit!!.position}",
            fruit.position in expectedZone
        )
    }

    @Test
    fun `fruit position is within spawn zone`() {
        // Given: ViewModel with initial state
        val gameState = viewModel.gameState.value
        val fruit = gameState.fruit

        // Then: fruit position should be valid
        assertNotNull("Fruit should be present", fruit)

        val gridSize = 15 // default from GameState.initial()
        assertTrue("Fruit X should be within bounds", fruit!!.position.x in 0 until gridSize)
        assertTrue("Fruit Y should be within bounds", fruit.position.y in 0 until gridSize)
    }

    @Test
    fun `fruit does not spawn on snake segments`() {
        // Given: ViewModel with initial state
        val gameState = viewModel.gameState.value
        val fruit = gameState.fruit

        // Then: fruit should not overlap with snake
        assertNotNull("Fruit should be present", fruit)

        val snakeSegments = listOf(gameState.snake.head) + gameState.snake.body
        assertTrue(
            "Fruit should not spawn on snake, but was at ${fruit!!.position}",
            fruit.position !in snakeSegments
        )
    }
}
