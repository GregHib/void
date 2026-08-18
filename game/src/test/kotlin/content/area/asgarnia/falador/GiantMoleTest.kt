package content.area.asgarnia.falador

import WorldTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class GiantMoleTest : WorldTest() {

    @Test
    fun `Thrown dirt blinds nearby players then clears itself`() {
        val tile = emptyTile
        val one = createPlayer(tile, name = "one")
        val two = createPlayer(tile.addX(2), name = "two")
        one.inventory.add("white_candle_lit")

        val script = scripts.filterIsInstance<GiantMole>().first()
        script.handleDirtOnScreen(tile)

        assertTrue(one.interfaces.contains("dirt_on_screen"), "nearest player should be blinded")
        assertTrue(two.interfaces.contains("dirt_on_screen"), "second player should be blinded")
        // Lit light sources are snuffed out by the dirt.
        assertTrue(one.inventory.contains("white_candle"), "lit candle should have been extinguished")

        tick(5)

        assertFalse(one.interfaces.contains("dirt_on_screen"), "dirt should clear on its own")
        assertFalse(two.interfaces.contains("dirt_on_screen"), "dirt should clear for every player")
    }
}
