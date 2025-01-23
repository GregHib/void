package world.gregs.voidps.type

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DeltaTest {

    @Test
    fun `Zero values`() {
        // Given
        val delta = Delta(0, 0, 0)
        // When
        val x = delta.x
        val y = delta.y
        val level = delta.level
        // Then
        assertEquals(17179607035, delta.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, level)
    }

    @Test
    fun `Negative values`() {
        // Given
        val delta = Delta(-10, -50, -2)
        // When
        val x = delta.x
        val y = delta.y
        val level = delta.level
        // Then
        assertEquals(-10, x)
        assertEquals(-50, y)
        assertEquals(-2, level)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val delta = Delta(32768, 32768, 3)
        // When
        val x = delta.x
        val y = delta.y
        val level = delta.level
        // Then
        assertEquals(32768, x)
        assertEquals(32768, y)
        assertEquals(3, level)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val delta = Delta(32769, 32769, 5)
        // When
        val x = delta.x
        val y = delta.y
        val level = delta.level
        // Then
        assertEquals(-32767, x)
        assertEquals(-32767, y)
        assertEquals(-3, level)
    }

    @Test
    fun `Add test`() {
        // Given
        val delta = Delta(0, 10, 1)
        // When
        val result = delta.add(1, 1, 1)
        val x = result.x
        val y = result.y
        val level = result.level
        // Then
        assertEquals(1, x)
        assertEquals(11, y)
        assertEquals(2, level)
        assertEquals(0, delta.x)
        assertEquals(10, delta.y)
        assertEquals(1, delta.level)
    }

    @Test
    fun `Vertical test`() {
        assertTrue(Delta(0, 10, 1).isVertical())
        assertTrue(Delta(0, -5, 0).isVertical())
        assertFalse(Delta(5, 0, 2).isVertical())
    }

    @Test
    fun `Horizontal test`() {
        assertTrue(Delta(10, 1, 1).isHorizontal())
        assertTrue(Delta(-5, 2, 0).isHorizontal())
        assertFalse(Delta(0, 3, 2).isHorizontal())
    }

    @Test
    fun `Cardinal test`() {
        assertTrue(Delta(10, 0, 1).isCardinal())
        assertTrue(Delta(-5, 0, 0).isCardinal())
        assertTrue(Delta(0, 10, 1).isCardinal())
        assertTrue(Delta(0, -5, 0).isCardinal())
        assertFalse(Delta(3, 3, 2).isCardinal())
        assertFalse(Delta(-5, 3, 2).isCardinal())
        assertFalse(Delta(1, -3, 2).isCardinal())
        assertFalse(Delta(-1, -3, 2).isCardinal())
    }

    @Test
    fun `Diagonal test`() {
        assertTrue(Delta(10, 11, 1).isDiagonal())
        assertTrue(Delta(-10, 11, 1).isDiagonal())
        assertTrue(Delta(10, -11, 1).isDiagonal())
        assertTrue(Delta(-5, -15, 1).isDiagonal())
        assertFalse(Delta(10, 0, 2).isDiagonal())
        assertFalse(Delta(-10, 0, 0).isDiagonal())
        assertFalse(Delta(0, 10, 1).isDiagonal())
        assertFalse(Delta(0, -10, 2).isDiagonal())
    }

    @Test
    fun `Invert test`() {
        // Given
        val delta = Delta(0, 10, 1)
        // When
        val result = delta.invert()
        val x = result.x
        val y = result.y
        val level = result.level
        // Then
        assertEquals(0, x)
        assertEquals(-10, y)
        assertEquals(-1, level)
    }

    @Test
    fun `Direction test`() {
        assertEquals(Direction.NORTH_WEST, Delta(-5, 15, 1).toDirection())
        assertEquals(Direction.NORTH, Delta(0, 11, 2).toDirection())
        assertEquals(Direction.NORTH_EAST, Delta(10, 11, 0).toDirection())
        assertEquals(Direction.EAST, Delta(10, 0, 1).toDirection())
        assertEquals(Direction.SOUTH_EAST, Delta(6, -11, 3).toDirection())
        assertEquals(Direction.SOUTH, Delta(0, -8, 0).toDirection())
        assertEquals(Direction.SOUTH_WEST, Delta(-1, -4, 2).toDirection())
        assertEquals(Direction.WEST, Delta(-5, 0, 1).toDirection())
    }

}