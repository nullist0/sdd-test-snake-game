package com.snakegame.domain.usecase

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import com.snakegame.domain.model.Direction
import com.snakegame.domain.model.SwipeGesture
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetectSwipeDirectionUseCaseTest {

    private lateinit var useCase: DetectSwipeDirectionUseCase
    private val density = Density(2f)  // 2x density for testing

    @Before
    fun setup() {
        useCase = DetectSwipeDirectionUseCase(density)
    }

    @Test
    fun `swipe right above threshold returns RIGHT`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(200f, 0f)  // Pure horizontal right
        )

        val result = useCase(gesture)

        assertEquals(Direction.RIGHT, result)
    }

    @Test
    fun `swipe left above threshold returns LEFT`() {
        val gesture = SwipeGesture(
            startPosition = Offset(200f, 0f),
            endPosition = Offset(0f, 0f)  // Pure horizontal left
        )

        val result = useCase(gesture)

        assertEquals(Direction.LEFT, result)
    }

    @Test
    fun `swipe up above threshold returns UP`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 200f),
            endPosition = Offset(0f, 0f)  // Pure vertical up (negative Y)
        )

        val result = useCase(gesture)

        assertEquals(Direction.UP, result)
    }

    @Test
    fun `swipe down above threshold returns DOWN`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(0f, 200f)  // Pure vertical down
        )

        val result = useCase(gesture)

        assertEquals(Direction.DOWN, result)
    }

    @Test
    fun `swipe below minimum distance returns null`() {
        // MIN_SWIPE_DISTANCE = 50.dp * 2 (density) = 100px
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(50f, 0f)  // Only 50px, below threshold
        )

        val result = useCase(gesture)

        assertNull(result)
    }

    @Test
    fun `diagonal swipe with horizontal dominance returns horizontal direction`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(200f, 80f)  // More horizontal (200 vs 80)
        )

        val result = useCase(gesture)

        assertEquals(Direction.RIGHT, result)
    }

    @Test
    fun `diagonal swipe with vertical dominance returns vertical direction`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(60f, 200f)  // More vertical (200 vs 60)
        )

        val result = useCase(gesture)

        assertEquals(Direction.DOWN, result)
    }

    @Test
    fun `exactly equal deltaX and deltaY defaults to vertical`() {
        val gesture = SwipeGesture(
            startPosition = Offset(0f, 0f),
            endPosition = Offset(150f, 150f)  // Perfect diagonal
        )

        val result = useCase(gesture)

        assertEquals(Direction.DOWN, result)  // Defaults to vertical
    }

    @Test
    fun `zero distance returns null`() {
        val gesture = SwipeGesture(
            startPosition = Offset(100f, 100f),
            endPosition = Offset(100f, 100f)  // No movement
        )

        val result = useCase(gesture)

        assertNull(result)
    }
}
