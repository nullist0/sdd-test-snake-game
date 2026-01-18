package com.snakegame.domain.model

import androidx.compose.ui.geometry.Offset

/**
 * Represents a swipe gesture with start and end positions.
 *
 * Provides methods to calculate gesture properties:
 * - Delta (change in position)
 * - Distance (magnitude of swipe)
 */
data class SwipeGesture(
    val startPosition: Offset,
    val endPosition: Offset
) {
    /**
     * The change in position from start to end.
     * Used for direction calculation.
     */
    val delta: Offset
        get() = endPosition - startPosition

    /**
     * The total distance of the swipe in pixels.
     * Used for threshold validation.
     */
    fun getDistance(): Float = delta.getDistance()

    /**
     * The horizontal component of the swipe.
     * Positive = right, Negative = left.
     */
    val deltaX: Float
        get() = delta.x

    /**
     * The vertical component of the swipe.
     * Positive = down, Negative = up.
     */
    val deltaY: Float
        get() = delta.y
}
