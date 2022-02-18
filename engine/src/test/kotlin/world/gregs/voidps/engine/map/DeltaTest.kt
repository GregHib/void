package world.gregs.voidps.engine.map

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DeltaTest {

    @Test
    fun `Zero values`() {
        // Given
        val delta = Delta(0, 0, 0)
        // When
        val x = delta.x
        val y = delta.y
        val plane = delta.plane
        // Then
        assertEquals(Tile(8192, 8192, 3).id, delta.id)
        assertEquals(0, x)
        assertEquals(0, y)
        assertEquals(0, plane)
    }

    @Test
    fun `Negative values`() {
        // Given
        val delta = Delta(-10, -50, -2)
        // When
        val x = delta.x
        val y = delta.y
        val plane = delta.plane
        // Then
        assertEquals(-10, x)
        assertEquals(-50, y)
        assertEquals(-2, plane)
    }

    @Test
    fun `Maximum values`() {
        // Given
        val delta = Delta(8191, 8191, 3)
        // When
        val x = delta.x
        val y = delta.y
        val plane = delta.plane
        // Then
        assertEquals(8191, x)
        assertEquals(8191, y)
        assertEquals(3, plane)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val delta = Delta(8192, 8192, 5)
        // When
        val x = delta.x
        val y = delta.y
        val plane = delta.plane
        // Then
        assertEquals(-8192, x)
        assertEquals(-8192, y)
        assertEquals(-3, plane)
    }

    @Test
    fun `Add test`() {
        // Given
        val delta = Delta(0, 10, 1)
        // When
        val result = delta.add(1, 1, 1)
        val x = result.x
        val y = result.y
        val plane = result.plane
        // Then
        assertEquals(1, x)
        assertEquals(11, y)
        assertEquals(2, plane)
        assertEquals(0, delta.x)
        assertEquals(10, delta.y)
        assertEquals(1, delta.plane)
    }

}