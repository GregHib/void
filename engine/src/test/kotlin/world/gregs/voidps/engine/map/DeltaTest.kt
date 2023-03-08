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
        assertEquals(17179607035, delta.id)
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
        val delta = Delta(32768, 32768, 3)
        // When
        val x = delta.x
        val y = delta.y
        val plane = delta.plane
        // Then
        assertEquals(32768, x)
        assertEquals(32768, y)
        assertEquals(3, plane)
    }

    @Test
    fun `Overflow values`() {
        // Given
        val delta = Delta(32769, 32769, 5)
        // When
        val x = delta.x
        val y = delta.y
        val plane = delta.plane
        // Then
        assertEquals(-32767, x)
        assertEquals(-32767, y)
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