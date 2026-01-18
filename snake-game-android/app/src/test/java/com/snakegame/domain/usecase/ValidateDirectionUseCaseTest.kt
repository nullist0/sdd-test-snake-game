package com.snakegame.domain.usecase

import com.snakegame.domain.model.Direction
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

/**
 * Parameterized unit tests for ValidateDirectionUseCase.
 *
 * Tests all 16 direction combinations (4 current × 4 requested):
 * - 4 reverse pairs (should reject - return false)
 * - 4 same direction (should accept - return true)
 * - 8 perpendicular combinations (should accept - return true)
 */
@RunWith(Parameterized::class)
class ValidateDirectionUseCaseTest(
    private val current: Direction,
    private val requested: Direction,
    private val expected: Boolean
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "current={0}, requested={1}, valid={2}")
        fun data() = listOf(
            // Reverse direction cases (should reject)
            arrayOf(Direction.UP, Direction.DOWN, false),
            arrayOf(Direction.DOWN, Direction.UP, false),
            arrayOf(Direction.LEFT, Direction.RIGHT, false),
            arrayOf(Direction.RIGHT, Direction.LEFT, false),

            // Same direction cases (should accept)
            arrayOf(Direction.UP, Direction.UP, true),
            arrayOf(Direction.DOWN, Direction.DOWN, true),
            arrayOf(Direction.LEFT, Direction.LEFT, true),
            arrayOf(Direction.RIGHT, Direction.RIGHT, true),

            // Perpendicular cases (should accept)
            arrayOf(Direction.UP, Direction.LEFT, true),
            arrayOf(Direction.UP, Direction.RIGHT, true),
            arrayOf(Direction.DOWN, Direction.LEFT, true),
            arrayOf(Direction.DOWN, Direction.RIGHT, true),
            arrayOf(Direction.LEFT, Direction.UP, true),
            arrayOf(Direction.LEFT, Direction.DOWN, true),
            arrayOf(Direction.RIGHT, Direction.UP, true),
            arrayOf(Direction.RIGHT, Direction.DOWN, true),
        )
    }

    private val useCase = ValidateDirectionUseCase()

    @Test
    fun `validates direction change according to reverse rule`() {
        val result = useCase(current, requested)
        assertEquals(
            expected,
            result,
            "Validation failed for $current → $requested (expected: $expected, got: $result)"
        )
    }
}

/**
 * Additional explicit tests for perpendicular direction acceptance.
 *
 * These tests complement the parameterized tests by explicitly naming
 * each perpendicular combination for clarity.
 */
class ValidateDirectionPerpendicularTest {

    private val useCase = ValidateDirectionUseCase()

    @Test
    fun `accepts all perpendicular directions from UP`() {
        assertTrue(useCase(Direction.UP, Direction.LEFT))
        assertTrue(useCase(Direction.UP, Direction.RIGHT))
    }

    @Test
    fun `accepts all perpendicular directions from DOWN`() {
        assertTrue(useCase(Direction.DOWN, Direction.LEFT))
        assertTrue(useCase(Direction.DOWN, Direction.RIGHT))
    }

    @Test
    fun `accepts all perpendicular directions from LEFT`() {
        assertTrue(useCase(Direction.LEFT, Direction.UP))
        assertTrue(useCase(Direction.LEFT, Direction.DOWN))
    }

    @Test
    fun `accepts all perpendicular directions from RIGHT`() {
        assertTrue(useCase(Direction.RIGHT, Direction.UP))
        assertTrue(useCase(Direction.RIGHT, Direction.DOWN))
    }
}

private fun assertTrue(condition: Boolean) {
    kotlin.test.assertTrue(condition)
}
