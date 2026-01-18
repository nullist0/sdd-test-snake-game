package com.snakegame.domain.usecase

import com.snakegame.domain.model.Position

/**
 * Calculates the 3x3 spawn zone centered on the snake's tail position.
 *
 * The zone includes the tail position and all 8 adjacent cells (3x3 grid).
 * Automatically clips positions that fall outside grid boundaries.
 *
 * Algorithm: tail ± 1 in both X and Y, filtered by grid bounds.
 * Performance: O(9) worst case (center position), O(4) best case (corner position).
 *
 * Example:
 * ```
 * Tail at (5,5) in 10x10 grid produces:
 * (4,4) (5,4) (6,4)
 * (4,5) (5,5) (6,5)  <- (5,5) is tail
 * (4,6) (5,6) (6,6)
 * ```
 */
class CalculateSpawnZoneUseCase {

    /**
     * Calculates the 3x3 zone around the tail position.
     *
     * @param tailPosition The position of the snake's tail
     * @param gridWidth Width of the game grid (exclusive upper bound)
     * @param gridHeight Height of the game grid (exclusive upper bound)
     * @return List of valid positions in the 3x3 zone (size: 1-9 depending on boundaries)
     */
    operator fun invoke(
        tailPosition: Position,
        gridWidth: Int,
        gridHeight: Int
    ): List<Position> {
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
}
