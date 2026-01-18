package com.snakegame.domain.usecase

import com.snakegame.domain.model.Position
import com.snakegame.ui.game.GameState
import kotlin.random.Random

/**
 * Orchestrates fruit spawning with 3x3 tail preference and grid fallback.
 *
 * Two-tier spawn strategy:
 * 1. **Tier 1 (Preferred)**: Try spawning in 3x3 zone around snake tail
 * 2. **Tier 2 (Fallback)**: If zone full, spawn anywhere on grid (added in Phase 4)
 *
 * This implementation (Phase 3 - US1) covers Tier 1 only.
 * Tier 2 fallback will be added in Phase 4 (US2).
 *
 * Performance: <50ms for 30x30 grid (typically <10ms for 10x10)
 *
 * @property calculateSpawnZone Use case to compute 3x3 tail-centered zone
 * @property findEmptyCells Use case to filter occupied positions
 * @property gridWidth Width of the game grid
 * @property gridHeight Height of the game grid
 * @property random Random number generator (injectable for testing)
 */
class SpawnFruitUseCase(
    private val calculateSpawnZone: CalculateSpawnZoneUseCase,
    private val findEmptyCells: FindEmptyCellsUseCase,
    private val gridWidth: Int,
    private val gridHeight: Int,
    private val random: Random = Random.Default
) {

    /**
     * Spawns a fruit based on current game state.
     *
     * @param gameState Current game state (snake position, existing fruit)
     * @return Position where fruit should spawn
     * @throws IllegalStateException if no empty cells available anywhere on grid
     */
    operator fun invoke(gameState: GameState): Position {
        // Get tail position (last segment of body, or head if body empty)
        val tailPosition = gameState.snake.body.lastOrNull() ?: gameState.snake.head
        val snakeSegments = listOf(gameState.snake.head) + gameState.snake.body

        // Tier 1: Try 3x3 tail-centered zone (US1 - preferred)
        val spawnZone = calculateSpawnZone(tailPosition, gridWidth, gridHeight)
        val emptyCellsInZone = findEmptyCells(spawnZone, snakeSegments, gameState.fruit)

        if (emptyCellsInZone.isNotEmpty()) {
            return emptyCellsInZone.random(random)
        }

        // Tier 2: Fallback to entire grid (US2)
        val allGridPositions = (0 until gridWidth).flatMap { x ->
            (0 until gridHeight).map { y -> Position(x, y) }
        }
        val emptyCellsInGrid = findEmptyCells(allGridPositions, snakeSegments, gameState.fruit)

        if (emptyCellsInGrid.isNotEmpty()) {
            return emptyCellsInGrid.random(random)
        }

        // No empty cells anywhere - game over scenario
        throw IllegalStateException("No empty cells on grid - game over")
    }
}
