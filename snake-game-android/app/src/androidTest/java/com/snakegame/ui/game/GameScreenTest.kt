package com.snakegame.ui.game

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.snakegame.domain.model.Direction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class GameScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun swipeGesture_updatesViewModelDirection() {
        val viewModel = GameViewModel()

        composeTestRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        // Initial direction should be RIGHT (default from existing code)
        val initialDirection = viewModel.gameState.value.snake.direction

        // Perform swipe up gesture
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(y = center.y - 300f),  // Swipe up
                    durationMillis = 200
                )
            }

        // Wait for state update
        composeTestRule.waitForIdle()

        // Verify direction changed to UP
        val updatedDirection = viewModel.gameState.value.snake.direction
        assertEquals(Direction.UP, updatedDirection)
    }

    @Test
    fun swipeRight_changesDirectionToRight() {
        val viewModel = GameViewModel()

        composeTestRule.setContent {
            GameScreen(viewModel = viewModel)
        }

        // Start by changing to UP direction
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(y = center.y - 300f),  // Swipe up first
                    durationMillis = 200
                )
            }

        composeTestRule.waitForIdle()

        // Now swipe right (perpendicular, should work)
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(x = center.x + 300f),  // Swipe right
                    durationMillis = 200
                )
            }

        composeTestRule.waitForIdle()

        // Verify direction is RIGHT
        assertEquals(Direction.RIGHT, viewModel.gameState.value.snake.direction)
    }
}
