package com.snakegame.domain.model

import androidx.compose.ui.geometry.Offset
import org.junit.Test
import kotlin.test.assertEquals

class SwipeGestureTest {

    @Test
    fun `delta calculates correctly from start to end position`() {
        // Given
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(150f, 250f)
        )

        // When
        val delta = gesture.delta

        // Then
        assertEquals(Offset(50f, 50f), delta)
    }

    @Test
    fun `getDistance calculates euclidean distance`() {
        // Given: 3-4-5 right triangle
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(3f, 4f)
        )

        // When
        val distance = gesture.getDistance()

        // Then
        assertEquals(5f, distance, 0.01f)
    }

    @Test
    fun `deltaX returns horizontal component`() {
        // Given
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(180f, 210f)
        )

        // When
        val deltaX = gesture.deltaX

        // Then
        assertEquals(80f, deltaX)
    }

    @Test
    fun `deltaY returns vertical component`() {
        // Given
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(110f, 350f)
        )

        // When
        val deltaY = gesture.deltaY

        // Then
        assertEquals(150f, deltaY)
    }

    @Test
    fun `negative delta for leftward swipe`() {
        // Given: swipe left
        val gesture = SwipeGesture(
            startPosition = Offset(200f, 100f),
            endPosition = Offset(50f, 100f)
        )

        // When
        val deltaX = gesture.deltaX

        // Then
        assertEquals(-150f, deltaX)
    }

    @Test
    fun `negative delta for upward swipe`() {
        // Given: swipe up (negative Y in Compose)
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 200f),
            endPosition = Offset(100f, 50f)
        )

        // When
        val deltaY = gesture.deltaY

        // Then
        assertEquals(-150f, deltaY)
    }
}
