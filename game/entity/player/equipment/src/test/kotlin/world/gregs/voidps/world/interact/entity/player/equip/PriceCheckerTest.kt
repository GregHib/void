package world.gregs.voidps.world.interact.entity.player.equip

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.world.community.trade.offer
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption
import world.gregs.voidps.world.script.walk

internal class PriceCheckerTest : WorldTest() {

    @Test
    fun `Check price of item`() {
        val player = createPlayer("player")
        player.inventory.add("bronze_sword")

        player.interfaceOption("worn_equipment", "price", "Show Price-checker")
        player.interfaceOption("price_checker_side", "items", "Add", 0, Item("bronze_sword", 1), slot = 0, inventory = "inventory")

        assertTrue(player.inventory[0].isEmpty())
        assertEquals(Item("bronze_sword", 1), player.offer[0])
    }

    @Test
    fun `Remove checked item`() {
        val player = createPlayer("player")
        player.inventory.add("bronze_sword")

        player.interfaceOption("worn_equipment", "price", "Show Price-checker")
        player.interfaceOption("price_checker_side", "items", "Add", 0, Item("bronze_sword", 1), slot = 0, inventory = "inventory")
        player.interfaceOption("price_checker", "items", "Remove-1", 0, Item("bronze_sword", 1), slot = 0, inventory = "trade_offer")

        assertEquals(Item("bronze_sword", 1), player.inventory[0])
        assertTrue(player.offer[0].isEmpty())
    }

    @Test
    fun `Return checked items on interface close`() {
        val player = createPlayer("player")
        player.inventory.add("bronze_sword")

        player.interfaceOption("worn_equipment", "price", "Show Price-checker")
        player.interfaceOption("price_checker_side", "items", "Add", 0, Item("bronze_sword", 1), slot = 0, inventory = "inventory")
        player.walk(player.tile.addX(1))
        tick()

        assertEquals(Item("bronze_sword", 1), player.inventory[0])
        assertTrue(player.offer.isEmpty())
    }
}