package com.snakegame.domain.model

/**
 * Represents the snake entity in the game.
 *
 * @property head The position of the snake's head
 * @property body The positions of all body segments (excluding head)
 * @property direction The current movement direction
 */
data class Snake(
    val head: Position,
    val body: List<Position>,
    val direction: Direction
) {
    companion object {
        /**
         * Creates the initial snake configuration for a new game.
         *
         * The snake starts:
         * - At the center of the grid
         * - With 1 head + 3 body segments = 4 total segments
         * - Moving RIGHT (default direction)
         *
         * @param gridSize The size of the game grid (default: 15x15)
         * @return A new Snake instance with initial configuration
         */
        fun initial(gridSize: Int = 15): Snake {
            val centerX = gridSize / 2
            val centerY = gridSize / 2

            return Snake(
                head = Position(centerX, centerY),
                body = listOf(
                    Position(centerX - 1, centerY),
                    Position(centerX - 2, centerY),
                    Position(centerX - 3, centerY)
                ),
                direction = Direction.RIGHT
            )
        }
    }
}
