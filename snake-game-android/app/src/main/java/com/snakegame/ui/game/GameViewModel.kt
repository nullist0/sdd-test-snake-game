package com.snakegame.ui.game

import androidx.lifecycle.ViewModel
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.Fruit
import com.snakegame.domain.usecase.CalculateSpawnZoneUseCase
import com.snakegame.domain.usecase.FindEmptyCellsUseCase
import com.snakegame.domain.usecase.SpawnFruitUseCase
import com.snakegame.domain.usecase.ValidateDirectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for the main game screen.
 *
 * Manages game state and handles player input with direction validation.
 * Uses ValidateDirectionUseCase to prevent reverse direction changes.
 * Handles fruit spawning with strategic 3x3 tail-centered placement.
 */
class GameViewModel(
    private val gridSize: Int = 15
) : ViewModel() {

    private val validateDirection = ValidateDirectionUseCase()
    private val spawnFruitUseCase = SpawnFruitUseCase(
        calculateSpawnZone = CalculateSpawnZoneUseCase(),
        findEmptyCells = FindEmptyCellsUseCase(),
        gridWidth = gridSize,
        gridHeight = gridSize
    )

    private val _gameState = MutableStateFlow(initialGameStateWithFruit())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /**
     * Creates initial game state with spawned fruit.
     */
    private fun initialGameStateWithFruit(): GameState {
        val baseState = GameState.initial(gridSize)
        val fruitPosition = spawnFruitUseCase(baseState)
        return baseState.copy(fruit = Fruit(fruitPosition))
    }

    /**
     * Handles directional input from player (e.g., swipe gesture).
     *
     * Validates the direction change using ValidateDirectionUseCase.
     * - Valid directions (perpendicular or same): Applied to snake
     * - Invalid directions (reverse): Silently ignored
     *
     * This prevents instant self-collision from reverse direction inputs.
     *
     * @param requestedDirection The direction requested by player input
     */
    fun handleDirectionInput(requestedDirection: Direction) {
        val currentDirection = _gameState.value.snake.direction

        // Validate direction change using use case
        val isValid = validateDirection(currentDirection, requestedDirection)

        if (isValid) {
            // Apply validated direction change
            _gameState.update { state ->
                state.copy(
                    snake = state.snake.copy(direction = requestedDirection)
                )
            }
        }
        // Invalid directions silently ignored (per spec requirement FR-003)
    }
}
