package com.snakegame.domain.model

/**
 * Represents a collectible fruit object in the snake game.
 *
 * The fruit spawns on the grid using strategic placement (3x3 tail-centered preference
 * with grid-wide fallback) and remains active until collected by the snake.
 *
 * @property position Grid coordinates where the fruit is located
 * @property isActive Whether the fruit can be collected (always true for this feature)
 */
data class Fruit(
    val position: Position,
    val isActive: Boolean = true
)
