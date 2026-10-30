package content.area.karamja.tai_bwo_wannai

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class HardwoodGroveTest : WorldTest() {

    @Test
    fun `Quick-pay takes trading sticks to enter the grove`() {
        val player = createPlayer(Tile(2816, 3083))
        player.inventory.add("trading_sticks", 150)
        val door = GameObjects.find(Tile(2817, 3083), "hardwood_grove_doors_closed")

        player.objectOption(door, "Quick-pay(100)")
        tick(6)

        // The doors sit on the west edge of the tile
        assertEquals(Tile(2817, 3083), player.tile)
        assertEquals(50, player.inventory.count("trading_sticks"))
        assertTrue(GameObjects.findOrNull(Tile(2817, 3083), "hardwood_grove_doors_closed") != null)
    }

    @Test
    fun `Can't enter the grove without trading sticks`() {
        val player = createPlayer(Tile(2816, 3083))
        val door = GameObjects.find(Tile(2817, 3083), "hardwood_grove_doors_closed")

        player.objectOption(door, "Quick-pay(100)")
        tick(3)

        assertEquals(Tile(2816, 3083), player.tile)
        assertTrue(player.containsMessage("You need 100 trading sticks"))
    }

    @Test
    fun `Leaving the grove is free`() {
        val player = createPlayer(Tile(2818, 3083))
        val door = GameObjects.find(Tile(2817, 3083), "hardwood_grove_doors_closed")

        player.objectOption(door, "Open")
        tick(6)

        assertEquals(Tile(2816, 3083), player.tile)
    }
}
