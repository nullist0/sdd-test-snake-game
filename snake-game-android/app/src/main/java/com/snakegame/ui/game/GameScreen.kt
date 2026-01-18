package com.snakegame.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

/**
 * Main game screen composable with swipe gesture detection.
 *
 * Renders the game UI and handles swipe input for directional control.
 * Integrates SwipeGestureDetector modifier with GameViewModel for state management.
 *
 * @param viewModel GameViewModel managing game state and direction changes
 * @param modifier Optional modifier for customization
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .swipeGestureDetector { direction ->
                viewModel.handleDirectionInput(direction)
            }
    ) {
        // TODO: Render snake, food, score, etc.
        // For now, just a basic container with gesture detection
        // Future features will add:
        // - SnakeRenderer(gameState.snake)
        // - FoodRenderer(gameState.food)
        // - ScoreDisplay(gameState.score)
        // - GameOverOverlay(gameState.isGameOver)
    }
}
