package world.gregs.voidps.engine.entity.character.mode.move

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile

class StepTest {

    @Test
    fun `Zero values`() {
        val tile = Tile(0, 0, 0)

        assertEquals(0, tile.id)
        assertEquals(0, tile.x)
        assertEquals(0, tile.y)
        assertEquals(0, tile.level)
        assertFalse(tile.noCollision)
        assertFalse(tile.noRun)
    }

    @Test
    fun `Negative values safe`() {
        val tile = Tile(-10, -50, -2).step(noCollision = false, noRun = true)

        assertEquals(16374, tile.x)
        assertEquals(16334, tile.y)
        assertEquals(2, tile.level)
        assertFalse(tile.noCollision)
        assertTrue(tile.noRun)
    }

    @Test
    fun `Maximum values`() {
        val tile = Tile(16320, 16320, 3).step(noCollision = true, noRun = true)

        assertEquals(16320, tile.x)
        assertEquals(16320, tile.y)
        assertEquals(3, tile.level)
        assertTrue(tile.noCollision)
        assertTrue(tile.noRun)
    }
}
