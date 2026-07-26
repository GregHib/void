package content.entity.npc.shop

import WorldTest
import interfaceOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

internal class ShopTest : WorldTest() {

    @Test
    fun `Buy item from the shop`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("bob", emptyTile.addY(4))
        player.inventory.add("coins", 1000)

        player.npcOption(npc, "Trade")
        tick(4)
        val shop = player.shopInventory(false)
        player.interfaceOption("shop", "stock", "Buy-1", item = Item("iron_battleaxe"), slot = 4 * 6)
        tick()

        assertTrue(player.inventory.count("coins") < 1000)
        assertEquals(1, player.inventory.count("iron_battleaxe"))
        assertEquals(9, shop.count("iron_battleaxe"))
    }

    @Test
    fun `Take free item from the shop`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("bob", emptyTile.addY(1))
        player.inventory.add("coins", 1000)

        player.npcOption(npc, "Trade")
        tick()
        val shop = player.shopInventory(true)
        player.interfaceOption("shop", "sample", "Take-1", item = Item("bronze_pickaxe"), slot = 0)

        assertEquals(1000, player.inventory.count("coins"))
        assertEquals(1, player.inventory.count("bronze_pickaxe"))
        assertEquals(0, shop.count("bronze_pickaxe"))
    }

    @Test
    fun `Sell item to the shop`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("bob", emptyTile.addY(1))
        player.inventory.add("iron_battleaxe", 1)

        player.npcOption(npc, "Trade")
        tick()
        val shop = player.shopInventory(false)
        player.interfaceOption("shop_side", "inventory", "Sell 1", item = Item("iron_battleaxe"), slot = 0)

        assertTrue(player.inventory.count("coins") > 0)
        assertEquals(11, shop.count("iron_battleaxe"))
    }

    @Test
    fun `Sell sample item back to depleted sample stock for free`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("bob", emptyTile.addY(1))
        player.inventory.add("bronze_hatchet", 1)

        player.npcOption(npc, "Trade")
        tick()
        val sample = player.shopInventory(true)
        val shop = player.shopInventory(false)
        sample.remove("bronze_hatchet", 1)
        assertEquals(0, sample.count("bronze_hatchet"))

        player.interfaceOption("shop_side", "inventory", "Sell 1", item = Item("bronze_hatchet"), slot = 0)

        assertEquals(0, player.inventory.count("coins"))
        assertEquals(1, sample.count("bronze_hatchet"))
        assertEquals(10, shop.count("bronze_hatchet"))
    }

    @Test
    fun `Sell sample item to main stock for coins when samples are full`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("bob", emptyTile.addY(1))
        player.inventory.add("bronze_hatchet", 1)

        player.npcOption(npc, "Trade")
        tick()
        val sample = player.shopInventory(true)
        val shop = player.shopInventory(false)
        player.interfaceOption("shop_side", "inventory", "Sell 1", item = Item("bronze_hatchet"), slot = 0)

        assertTrue(player.inventory.count("coins") > 0)
        assertEquals(1, sample.count("bronze_hatchet"))
        assertEquals(11, shop.count("bronze_hatchet"))
    }

    @Test
    fun `Selling more than the sample deficit sells the remainder for coins`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("bob", emptyTile.addY(1))
        player.inventory.add("bronze_hatchet", 2)

        player.npcOption(npc, "Trade")
        tick()
        val sample = player.shopInventory(true)
        val shop = player.shopInventory(false)
        sample.remove("bronze_hatchet", 1)
        assertEquals(0, sample.count("bronze_hatchet"))

        player.interfaceOption("shop_side", "inventory", "Sell 5", item = Item("bronze_hatchet"), slot = 0)

        assertTrue(player.inventory.count("coins") > 0)
        assertEquals(0, player.inventory.count("bronze_hatchet"))
        assertEquals(1, sample.count("bronze_hatchet"))
        assertEquals(11, shop.count("bronze_hatchet"))
    }

    @Test
    fun `Can't sell coins to a general store`() {
        val player = createPlayer(emptyTile)
        val npc = createNPC("shopkeeper_lumbridge", emptyTile.addY(1))
        player.inventory.add("coins", 100)

        player.npcOption(npc, "Trade")
        tick()
        val shop = player.shopInventory(false)
        player.interfaceOption("shop_side", "inventory", "Sell 1", item = Item("coins"), slot = 0)

        assertEquals(100, player.inventory.count("coins"))
        assertEquals(0, shop.count("coins"))
    }
}
