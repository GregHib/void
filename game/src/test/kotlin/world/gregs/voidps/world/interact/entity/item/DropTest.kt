package world.gregs.voidps.world.interact.entity.item

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.FloorItems
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.floorItemOption
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.mockStackableItem

internal class DropTest : WorldMock() {

    private lateinit var floorItems: FloorItems

    @BeforeEach
    fun start() {
        floorItems = get()
    }

    @Test
    fun `Drop item onto the floor`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player")
        player.inventory.add("bronze_sword")

        player.interfaceOption("inventory", "container", "Drop", 4, Item("bronze_sword", 1), 0)

        assertTrue(player.inventory.isEmpty())
        assertTrue(floorItems[player.tile].any { it.name == "bronze_sword" })
    }

    @Test
    fun `Pickup item off the floor`() = runBlocking(Dispatchers.Default) {
        val tile = Tile(100, 100)
        val player = createPlayer("player", tile)
        val item = floorItems.add("bronze_sword", 1, tile.add(0, 2))!!
        player.inventory.add("bronze_sword")

        player.floorItemOption(item, "Take")
        tick()

        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(floorItems[tile.add(0, 2)].isEmpty())
    }

    @Test
    fun `Drop stackable items on one another`() = runBlocking(Dispatchers.Default) {
        mockStackableItem(995) // coins
        val tile = Tile(100, 100)
        val player = createPlayer("player", tile)
        floorItems.add("coins", 500, tile)!!
        player.inventory.add("coins", 500)

        player.interfaceOption("inventory", "container", "Drop", 4, Item("coins", 500), 0)

        assertTrue(player.inventory.isEmpty())
        assertEquals(1, floorItems[tile].count { it.name == "coins" })
        assertEquals(1000, floorItems[tile].first().amount)
    }

    @Test
    fun `Drop items on one another`() = runBlocking(Dispatchers.Default) {
        val tile = Tile(100, 100)
        val player = createPlayer("player", tile)
        floorItems.add("bronze_sword", 1, tile)!!
        player.inventory.add("bronze_sword")

        player.interfaceOption("inventory", "container", "Drop", 4, Item("bronze_sword", 1), 0)

        assertTrue(player.inventory.isEmpty())
        assertEquals(2, floorItems[tile].count { it.name == "bronze_sword" })
    }

}