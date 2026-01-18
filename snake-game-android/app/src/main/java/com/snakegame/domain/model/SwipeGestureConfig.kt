package com.snakegame.domain.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Configuration for swipe gesture detection thresholds.
 *
 * These values are tuned based on:
 * - Android touch guidelines (48dp minimum touch target)
 * - User testing feedback
 * - Performance requirements (FR-002: <100ms latency)
 */
object SwipeGestureConfig {
    /**
     * Minimum swipe distance in density-independent pixels.
     *
     * Swipes shorter than this are ignored to prevent accidental touches.
     * Based on research: 50-100dp is optimal for mobile games.
     */
    val MIN_SWIPE_DISTANCE: Dp = 50.dp

    /**
     * Optional: Debounce time in milliseconds.
     *
     * Minimum time between direction changes to prevent input spam.
     * May not be needed if game loop already limits direction changes.
     */
    const val DEBOUNCE_TIME_MS: Long = 100L

    /**
     * Optional: Maximum queued direction changes.
     *
     * For FR-006 if implemented. Set to 0 to disable queue.
     */
    const val MAX_QUEUED_DIRECTIONS: Int = 0 // Start simple, add if needed
}
