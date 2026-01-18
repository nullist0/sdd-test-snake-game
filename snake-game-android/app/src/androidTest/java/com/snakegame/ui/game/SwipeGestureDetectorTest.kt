package com.snakegame.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.snakegame.domain.model.Direction
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SwipeGestureDetectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun swipeRight_aboveThreshold_invokesCallbackWithRightDirection() {
        var capturedDirection: Direction? = null

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                    }
            )
        }

        // Perform swipe right
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(x = center.x + 300f),  // Swipe right 300px
                    durationMillis = 200
                )
            }

        // Verify
        assertEquals(Direction.RIGHT, capturedDirection)
    }

    @Test
    fun swipeLeft_aboveThreshold_invokesCallbackWithLeftDirection() {
        var capturedDirection: Direction? = null

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                    }
            )
        }

        // Perform swipe left
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(x = center.x - 300f),  // Swipe left 300px
                    durationMillis = 200
                )
            }

        // Verify
        assertEquals(Direction.LEFT, capturedDirection)
    }

    @Test
    fun swipeUp_aboveThreshold_invokesCallbackWithUpDirection() {
        var capturedDirection: Direction? = null

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                    }
            )
        }

        // Perform swipe up
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(y = center.y - 300f),  // Swipe up 300px
                    durationMillis = 200
                )
            }

        // Verify
        assertEquals(Direction.UP, capturedDirection)
    }

    @Test
    fun swipeDown_aboveThreshold_invokesCallbackWithDownDirection() {
        var capturedDirection: Direction? = null

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                    }
            )
        }

        // Perform swipe down
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(y = center.y + 300f),  // Swipe down 300px
                    durationMillis = 200
                )
            }

        // Verify
        assertEquals(Direction.DOWN, capturedDirection)
    }

    @Test
    fun shortSwipe_belowThreshold_doesNotInvokeCallback() {
        var capturedDirection: Direction? = null
        var callbackInvoked = false

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                        callbackInvoked = true
                    }
            )
        }

        // Perform very short swipe (below 50dp threshold)
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(x = center.x + 20f),  // Only 20px
                    durationMillis = 100
                )
            }

        // Verify callback was NOT invoked
        assertEquals(false, callbackInvoked)
        assertNull(capturedDirection)
    }

    @Test
    fun diagonalSwipe_horizontalDominant_resolvesToHorizontalDirection() {
        var capturedDirection: Direction? = null

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                    }
            )
        }

        // Perform diagonal swipe with stronger horizontal component
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(x = center.x + 300f, y = center.y + 100f),
                    durationMillis = 200
                )
            }

        // Verify horizontal direction wins
        assertEquals(Direction.RIGHT, capturedDirection)
    }

    @Test
    fun diagonalSwipe_verticalDominant_resolvesToVerticalDirection() {
        var capturedDirection: Direction? = null

        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .swipeGestureDetector { direction ->
                        capturedDirection = direction
                    }
            )
        }

        // Perform diagonal swipe with stronger vertical component
        composeTestRule.onRoot()
            .performTouchInput {
                swipe(
                    start = center,
                    end = center.copy(x = center.x + 100f, y = center.y + 300f),
                    durationMillis = 200
                )
            }

        // Verify vertical direction wins
        assertEquals(Direction.DOWN, capturedDirection)
    }
}
