package com.snakegame.ui.game

import com.snakegame.domain.model.Fruit
import com.snakegame.domain.model.Snake

/**
 * Represents the complete game state.
 *
 * @property snake The current snake entity (position, body, direction)
 * @property fruit The current fruit entity (null if no fruit present)
 * @property score The current score (snake length)
 * @property isGameOver Whether the game has ended
 */
data class GameState(
    val snake: Snake,
    val fruit: Fruit? = null,
    val score: Int = 0,
    val isGameOver: Boolean = false
) {
    companion object {
        /**
         * Creates the initial game state for a new game.
         *
         * @param gridSize The size of the game grid (default: 15x15)
         * @return A new GameState with initial configuration
         */
        fun initial(gridSize: Int = 15): GameState {
            val snake = Snake.initial(gridSize)
            return GameState(
                snake = snake,
                score = snake.body.size + 1, // Total length: head + body
                isGameOver = false
            )
        }
    }
}
