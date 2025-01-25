package world.gregs.voidps.world.interact.entity.npc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.entity.item.Item
import content.entity.npc.shop.shopInventory
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.npcOption

internal class ShopTest : WorldTest() {

    @Test
    fun `Buy item from the shop`() {
        val player = createPlayer("shopper", emptyTile)
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
        val player = createPlayer("shopper", emptyTile)
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
        val player = createPlayer("shopper", emptyTile)
        val npc = createNPC("bob", emptyTile.addY(1))
        player.inventory.add("iron_battleaxe", 1)

        player.npcOption(npc, "Trade")
        tick()
        val shop = player.shopInventory(false)
        player.interfaceOption("shop_side", "inventory", "Sell 1", item = Item("iron_battleaxe"), slot = 0)

        assertTrue(player.inventory.count("coins") > 0)
        assertEquals(11, shop.count("iron_battleaxe"))
    }

}