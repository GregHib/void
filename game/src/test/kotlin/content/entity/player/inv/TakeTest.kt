package content.entity.player.inv

import WorldTest
import floorItemOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal class TakeTest : WorldTest() {

    @Test
    fun `Take item off the floor`() {
        val tile = emptyTile
        val player = createPlayer("player", tile)
        val item = floorItems.add(tile.add(0, 2), "bronze_sword")

        player.floorItemOption(item, "Take")
        tick(5)

        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(floorItems[tile.add(0, 2)].isEmpty())
    }

    @Test
    fun `Take item up off a table`() {
        val tile = Tile(3212, 3218, 1)
        val player = createPlayer("player", tile)
        val item = floorItems.add(tile.add(1, 0), "bronze_sword")

        player.floorItemOption(item, "Take")
        tick(5)

        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(floorItems[tile.add(1, 0)].isEmpty())
        assertEquals(tile, player.tile)
    }

}