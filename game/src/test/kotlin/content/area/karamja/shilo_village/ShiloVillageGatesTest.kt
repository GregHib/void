package content.area.karamja.shilo_village

import WorldTest
import dialogueOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import skipDialogues
import walk
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class ShiloVillageGatesTest : WorldTest() {

    @Test
    fun `Climb over the broken cart into the village`() {
        val player = createPlayer(Tile(2880, 2952))
        val cart = GameObjects.find(Tile(2877, 2951), "broken_cart_shilo_village")

        player.objectOption(cart, "Look-at")
        tick()
        player.skipDialogues()
        player.dialogueOption(1)
        tick(3)

        assertEquals(Tile(2876, 2952), player.tile)
    }

    @Test
    fun `Climb back over the broken cart`() {
        val player = createPlayer(Tile(2876, 2952))
        val cart = GameObjects.find(Tile(2877, 2951), "broken_cart_shilo_village")

        player.objectOption(cart, "Search")
        tick()
        player.skipDialogues()
        player.dialogueOption(1)
        tick(3)

        assertEquals(Tile(2880, 2952), player.tile)
    }

    @Test
    fun `Open walk through and close the metal gate`() {
        val player = createPlayer(Tile(2876, 2952))
        val gate = GameObjects.find(Tile(2875, 2952), "metal_gate_closed")

        player.objectOption(gate, "Open")
        tick(5)

        val opened = (2873..2877).flatMap { x -> (2950..2955).mapNotNull { y -> GameObjects.findOrNull(Tile(x, y), "gate_29_opened") } }.firstOrNull()
        assertNotNull(opened)

        player.walk(Tile(2873, 2952))
        tick(6)
        assertEquals(Tile(2873, 2952), player.tile)

        player.objectOption(opened!!, "Close")
        tick(3)

        assertNotNull(GameObjects.findOrNull(Tile(2875, 2952), "metal_gate_closed"))
        assertNotNull(GameObjects.findOrNull(Tile(2875, 2953), "metal_gate_2_closed"))
    }

    @Test
    fun `Wooden gate lets players through and closes behind them`() {
        val player = createPlayer(Tile(2866, 2953))
        val gate = GameObjects.find(Tile(2867, 2953), "wooden_gate_closed")

        player.objectOption(gate, "Open")
        tick(8)

        assertEquals(Tile(2868, 2953), player.tile)
        assertNotNull(GameObjects.findOrNull(Tile(2867, 2953), "wooden_gate_closed"))
    }
}
