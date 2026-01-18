package com.snakegame.ui.game

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.SwipeGesture
import com.snakegame.domain.model.SwipeGestureConfig
import com.snakegame.domain.usecase.DetectSwipeDirectionUseCase

/**
 * Composable modifier that detects swipe gestures and invokes callback with detected direction.
 *
 * Uses Compose gesture detection APIs (detectDragGestures) to capture touch events,
 * accumulates drag deltas, and converts to cardinal directions using DetectSwipeDirectionUseCase.
 *
 * @param minSwipeDistance Minimum swipe distance required to trigger direction change
 * @param onSwipe Callback invoked when valid swipe is detected with the calculated direction
 * @return Modified Modifier with gesture detection capability
 */
@Composable
fun Modifier.swipeGestureDetector(
    minSwipeDistance: Dp = SwipeGestureConfig.MIN_SWIPE_DISTANCE,
    onSwipe: (Direction) -> Unit
): Modifier {
    val density = LocalDensity.current
    val detectSwipeDirection = remember { DetectSwipeDirectionUseCase(density) }

    var accumulatedDrag by remember { mutableStateOf(Offset.Zero) }

    return this.pointerInput(Unit) {
        detectDragGestures(
            onDragStart = {
                accumulatedDrag = Offset.Zero
            },
            onDrag = { change, dragAmount ->
                change.consume()
                accumulatedDrag += dragAmount
            },
            onDragEnd = {
                val gesture = SwipeGesture(
                    startPosition = Offset.Zero,
                    endPosition = accumulatedDrag
                )

                val direction = detectSwipeDirection(gesture)
                if (direction != null) {
                    onSwipe(direction)
                }
            }
        )
    }
}
