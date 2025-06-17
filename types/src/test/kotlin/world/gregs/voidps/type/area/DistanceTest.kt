package world.gregs.voidps.type.area

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile

internal class DistanceTest {

    @Test
    fun `Distance between two identical tiles`() {
        val tile = Tile(0, 0)
        val other = Tile(0, 0)
        // Then
        assertEquals(0, tile.distanceTo(other))
    }

    @Test
    fun `Distance between two diagonal tiles`() {
        val tile = Tile(1, 1)
        val other = Tile(3, 3)
        // Then
        assertEquals(2, tile.distanceTo(other))
        assertEquals(2, other.distanceTo(tile))
    }

    @Test
    fun `Distance between two horizontal tiles`() {
        val tile = Tile(1, 0)
        val other = Tile(3, 0)
        // Then
        assertEquals(2, tile.distanceTo(other))
    }

    @Test
    fun `Distance between two vertical tiles`() {
        val tile = Tile(0, 1)
        val other = Tile(0, 3)
        // Then
        assertEquals(2, tile.distanceTo(other))
    }

    @Test
    fun `Distance between two tiles`() {
        val tile = Tile(1, 2)
        val other = Tile(4, 3)
        // Then
        assertEquals(3, tile.distanceTo(other))
    }

    @Test
    fun `Distance on two levels is invalid`() {
        val tile = Tile(0, 0, 0)
        val other = Tile(0, 0, 1)
        // Then
        assertEquals(-1, tile.distanceTo(other))
    }
}
