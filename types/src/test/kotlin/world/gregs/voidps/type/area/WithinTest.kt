package world.gregs.voidps.type.area

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile

internal class WithinTest {

    @Test
    fun `Within radius on identical tile`() {
        val tile = Tile(0, 0)
        val other = Tile(0, 0)
        // Then
        assertTrue(tile.within(other, radius = 1))
        assertTrue(tile.within(other, radius = 0))
    }

    @Test
    fun `Within radius diagonal`() {
        val tile = Tile(0, 0)
        val other = Tile(1, 1)
        // Then
        assertTrue(tile.within(other, radius = 2))
        assertTrue(tile.within(other, radius = 1))
        assertFalse(tile.within(other, radius = 0))
    }

    @Test
    fun `Within radius horizontal`() {
        val tile = Tile(1, 0)
        val other = Tile(3, 0)
        // Then
        assertTrue(tile.within(other, radius = 3))
        assertTrue(tile.within(other, radius = 2))
        assertFalse(tile.within(other, radius = 1))
    }

    @Test
    fun `Within radius vertical`() {
        val tile = Tile(0, 4)
        val other = Tile(0, 1)
        // Then
        assertTrue(tile.within(other, radius = 4))
        assertTrue(tile.within(other, radius = 3))
        assertFalse(tile.within(other, radius = 2))
    }
}
