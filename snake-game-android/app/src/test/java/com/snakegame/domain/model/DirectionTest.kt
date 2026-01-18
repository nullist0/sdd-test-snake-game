package com.snakegame.domain.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for Direction enum.
 *
 * Tests the reverse direction logic and perpendicular direction detection
 * that are core to the reverse direction prevention feature.
 */
class DirectionTest {

    @Test
    fun `reverse returns opposite direction for all directions`() {
        assertEquals(Direction.DOWN, Direction.UP.reverse())
        assertEquals(Direction.UP, Direction.DOWN.reverse())
        assertEquals(Direction.RIGHT, Direction.LEFT.reverse())
        assertEquals(Direction.LEFT, Direction.RIGHT.reverse())
    }

    @Test
    fun `isReverse returns true for opposite directions`() {
        assertTrue(Direction.UP.isReverse(Direction.DOWN))
        assertTrue(Direction.DOWN.isReverse(Direction.UP))
        assertTrue(Direction.LEFT.isReverse(Direction.RIGHT))
        assertTrue(Direction.RIGHT.isReverse(Direction.LEFT))
    }

    @Test
    fun `isReverse returns false for perpendicular directions`() {
        assertFalse(Direction.UP.isReverse(Direction.LEFT))
        assertFalse(Direction.UP.isReverse(Direction.RIGHT))
        assertFalse(Direction.DOWN.isReverse(Direction.LEFT))
        assertFalse(Direction.DOWN.isReverse(Direction.RIGHT))
        assertFalse(Direction.LEFT.isReverse(Direction.UP))
        assertFalse(Direction.LEFT.isReverse(Direction.DOWN))
        assertFalse(Direction.RIGHT.isReverse(Direction.UP))
        assertFalse(Direction.RIGHT.isReverse(Direction.DOWN))
    }

    @Test
    fun `isReverse returns false for same direction`() {
        Direction.values().forEach { direction ->
            assertFalse(direction.isReverse(direction))
        }
    }

    @Test
    fun `same direction is not reverse - explicit validation`() {
        // User Story 3: Same direction inputs should be accepted
        // This verifies that isReverse() returns false for same direction
        assertFalse(Direction.UP.isReverse(Direction.UP))
        assertFalse(Direction.DOWN.isReverse(Direction.DOWN))
        assertFalse(Direction.LEFT.isReverse(Direction.LEFT))
        assertFalse(Direction.RIGHT.isReverse(Direction.RIGHT))
    }

    @Test
    fun `isPerpendicular returns true for 90 degree directions`() {
        // From UP
        assertTrue(Direction.UP.isPerpendicular(Direction.LEFT))
        assertTrue(Direction.UP.isPerpendicular(Direction.RIGHT))

        // From DOWN
        assertTrue(Direction.DOWN.isPerpendicular(Direction.LEFT))
        assertTrue(Direction.DOWN.isPerpendicular(Direction.RIGHT))

        // From LEFT
        assertTrue(Direction.LEFT.isPerpendicular(Direction.UP))
        assertTrue(Direction.LEFT.isPerpendicular(Direction.DOWN))

        // From RIGHT
        assertTrue(Direction.RIGHT.isPerpendicular(Direction.UP))
        assertTrue(Direction.RIGHT.isPerpendicular(Direction.DOWN))
    }

    @Test
    fun `isPerpendicular returns false for reverse and same directions`() {
        // Reverse
        assertFalse(Direction.UP.isPerpendicular(Direction.DOWN))
        assertFalse(Direction.DOWN.isPerpendicular(Direction.UP))
        assertFalse(Direction.LEFT.isPerpendicular(Direction.RIGHT))
        assertFalse(Direction.RIGHT.isPerpendicular(Direction.LEFT))

        // Same
        assertFalse(Direction.UP.isPerpendicular(Direction.UP))
        assertFalse(Direction.DOWN.isPerpendicular(Direction.DOWN))
        assertFalse(Direction.LEFT.isPerpendicular(Direction.LEFT))
        assertFalse(Direction.RIGHT.isPerpendicular(Direction.RIGHT))
    }
}
