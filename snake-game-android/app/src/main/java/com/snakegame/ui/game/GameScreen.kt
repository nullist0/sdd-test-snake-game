package com.snakegame.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Main game screen composable with swipe gesture detection and fruit rendering.
 *
 * Renders the game UI and handles swipe input for directional control.
 * Integrates SwipeGestureDetector modifier with GameViewModel for state management.
 * Displays fruit using FruitRenderer when present.
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

    // Calculate cell size based on grid dimensions
    // Using screen dimensions and grid size (15x15 default)
    val density = LocalDensity.current
    val gridSize = 15 // TODO: get from viewModel or config
    val cellSize = with(density) {
        // This is a placeholder - actual cell size should be calculated from screen dimensions
        // For now, using a fixed size for rendering
        24.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .swipeGestureDetector { direction ->
                viewModel.handleDirectionInput(direction)
            }
    ) {
        // Render fruit
        FruitRenderer(
            fruit = gameState.fruit,
            cellSize = cellSize,
            modifier = Modifier.fillMaxSize()
        )

        // TODO: Render other game elements
        // Future features will add:
        // - SnakeRenderer(gameState.snake, cellSize)
        // - ScoreDisplay(gameState.score)
        // - GameOverOverlay(gameState.isGameOver)
    }
}
