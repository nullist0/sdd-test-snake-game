package com.snakegame.domain.usecase

import com.snakegame.domain.model.Fruit
import com.snakegame.domain.model.Position

/**
 * Finds empty cells from a list of candidate positions.
 *
 * Filters out positions occupied by the snake or existing fruit.
 * Uses set-based lookup for O(1) containment checks.
 *
 * Performance: O(n + m) where n = snake length, m = candidate count
 * Typical: O(20 + 9) for 20-segment snake with 3x3 zone
 */
class FindEmptyCellsUseCase {

    /**
     * Filters candidate positions to find unoccupied cells.
     *
     * @param candidates List of positions to check (e.g., 3x3 spawn zone or entire grid)
     * @param snakeSegments All positions occupied by the snake (head + body)
     * @param existingFruit Current fruit (null if no fruit present)
     * @return List of positions not occupied by snake or fruit
     */
    operator fun invoke(
        candidates: List<Position>,
        snakeSegments: List<Position>,
        existingFruit: Fruit?
    ): List<Position> {
        // Create set of occupied positions for O(1) lookup
        val occupiedPositions = snakeSegments.toSet() + listOfNotNull(existingFruit?.position)

        // Filter candidates to exclude occupied positions
        return candidates.filter { it !in occupiedPositions }
    }
}
