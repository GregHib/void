package content.entity.player.inv

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import WorldTest
import floorItemOption
import interfaceOption
import itemOnObject
import kotlin.test.assertFalse

internal class DropTest : WorldTest() {

    @Test
    fun `Drop item onto the floor`() {
        val player = createPlayer("player")
        player.inventory.add("bronze_sword")

        player.interfaceOption("inventory", "inventory", "Drop", 4, Item("bronze_sword"), 0)

        assertTrue(player.inventory.isEmpty())
        assertTrue(floorItems[player.tile].any { it.id == "bronze_sword" })
    }

    @Test
    fun `Pickup item off the floor`() {
        val tile = emptyTile
        val player = createPlayer("player", tile)
        val item = floorItems.add(tile.add(0, 2), "bronze_sword")

        player.floorItemOption(item, "Take")
        tick(5)

        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(floorItems[tile.add(0, 2)].isEmpty())
    }

    @Test
    fun `Floor item respawns after delay`() {
        val tile = Tile(3244, 3157)
        val player = createPlayer("player", tile)

        val floorItem = floorItems[tile].first()
        player.floorItemOption(floorItem, "Take")
        tick(1)

        assertTrue(player.inventory.contains("small_fishing_net"))
        tick(10)
        assertTrue(floorItems[tile].isNotEmpty())
    }

    @Test
    fun `Drop stackable items on one another`() {
        val tile = emptyTile
        val player = createPlayer("player", tile)
        floorItems.add(tile, "coins", 500, owner = player)
        player.inventory.add("coins", 500)

        player.interfaceOption("inventory", "inventory", "Drop", 4, Item("coins", 500), 0)

        assertTrue(player.inventory.isEmpty())
        assertEquals(1, floorItems[tile].count { it.id == "coins" })
        assertEquals(1000, floorItems[tile].first().amount)
    }

    @Test
    fun `Drop items on one another`() {
        val tile = emptyTile
        val player = createPlayer("player", tile)
        floorItems.add(tile, "bronze_sword")
        player.inventory.add("bronze_sword")

        player.interfaceOption("inventory", "inventory", "Drop", 4, Item("bronze_sword"), 0)

        assertTrue(player.inventory.isEmpty())
        assertEquals(2, floorItems[tile].count { it.id == "bronze_sword" })
    }

    @Test
    fun `Place item onto a table`() {
        val tile = Tile(3212, 3218, 1)
        val player = createPlayer("player", tile)
        player.inventory.add("bronze_sword")
        val drawers = objects[tile.addX(1), "table_lumbridge"]!!
        player.itemOnObject(drawers, itemSlot = 0, id = "bronze_sword")

        assertTrue(player.inventory.isEmpty())
        assertTrue(floorItems[tile.addX(1)].any { it.id == "bronze_sword" })
    }

    @Test
    fun `Can't place un-tradeable item onto a table`() {
        val tile = Tile(3212, 3218, 1)
        val player = createPlayer("player")
        player.inventory.add("toolkit")
        val drawers = objects[tile.addX(1), "table_lumbridge"]!!

        player.itemOnObject(drawers, itemSlot = 0, id = "toolkit")

        assertTrue(player.inventory.contains("toolkit"))
        assertFalse(floorItems[tile.addX(1)].any { it.id == "toolkit" })
    }

    @Test
    fun `Pickup item up off a table`() {
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