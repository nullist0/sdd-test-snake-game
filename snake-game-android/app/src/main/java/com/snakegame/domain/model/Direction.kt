package com.snakegame.domain.model

/**
 * Represents the four cardinal directions for snake movement.
 *
 * Provides methods to determine direction relationships:
 * - Reverse (180° opposite)
 * - Perpendicular (90° turns)
 */
enum class Direction {
    UP, DOWN, LEFT, RIGHT;

    /**
     * Returns the reverse (opposite) direction.
     *
     * Direction pairs:
     * - UP ↔ DOWN
     * - LEFT ↔ RIGHT
     *
     * @return The opposite direction
     */
    fun reverse(): Direction = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    /**
     * Checks if another direction is the reverse of this direction.
     *
     * Used for validation: reverse directions should be rejected
     * to prevent instant self-collision.
     *
     * @param other The direction to check
     * @return true if other is the reverse direction, false otherwise
     */
    fun isReverse(other: Direction): Boolean =
        this.reverse() == other

    /**
     * Checks if another direction is perpendicular to this direction.
     *
     * Perpendicular directions (90° turns):
     * - From UP/DOWN: LEFT or RIGHT
     * - From LEFT/RIGHT: UP or DOWN
     *
     * @param other The direction to check
     * @return true if other is perpendicular, false otherwise
     */
    fun isPerpendicular(other: Direction): Boolean =
        this != other && !isReverse(other)
}
