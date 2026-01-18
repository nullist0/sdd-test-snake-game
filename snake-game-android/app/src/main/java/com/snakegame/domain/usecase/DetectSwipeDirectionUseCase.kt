package com.snakegame.domain.usecase

import androidx.compose.ui.unit.Density
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.SwipeGesture
import com.snakegame.domain.model.SwipeGestureConfig
import kotlin.math.abs

/**
 * Converts SwipeGesture data into a cardinal Direction, applying threshold validation.
 *
 * Business logic for detecting swipe direction from gesture data.
 * Uses dominant axis algorithm to resolve diagonal swipes.
 */
class DetectSwipeDirectionUseCase(
    private val density: Density
) {
    /**
     * Detects direction from a swipe gesture.
     *
     * @param gesture The swipe gesture to analyze
     * @return Direction if gesture meets threshold, null if too short
     */
    operator fun invoke(gesture: SwipeGesture): Direction? {
        // Convert min distance from Dp to pixels
        val minDistancePx = with(density) {
            SwipeGestureConfig.MIN_SWIPE_DISTANCE.toPx()
        }

        // Check threshold
        if (gesture.getDistance() < minDistancePx) {
            return null
        }

        // Determine direction based on dominant axis
        val deltaX = gesture.deltaX
        val deltaY = gesture.deltaY

        return when {
            abs(deltaX) > abs(deltaY) -> {
                // Horizontal dominates
                if (deltaX > 0) Direction.RIGHT else Direction.LEFT
            }
            else -> {
                // Vertical dominates (or equal - default to vertical)
                if (deltaY > 0) Direction.DOWN else Direction.UP
            }
        }
    }
}
