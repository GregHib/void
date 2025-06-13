package content.entity.player.kept

import WorldTest
import content.social.trade.offer
import interfaceOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import walk
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

internal class PriceCheckerTest : WorldTest() {

    @Test
    fun `Check price of item`() {
        val player = createPlayer()
        player.inventory.add("bronze_sword")

        player.interfaceOption("worn_equipment", "price", "Show Price-checker")
        player.interfaceOption("price_checker_side", "items", "Add", 0, Item("bronze_sword"), slot = 0)

        assertTrue(player.inventory[0].isEmpty())
        assertEquals(Item("bronze_sword"), player.offer[0])
    }

    @Test
    fun `Remove checked item`() {
        val player = createPlayer()
        player.inventory.add("bronze_sword")

        player.interfaceOption("worn_equipment", "price", "Show Price-checker")
        player.interfaceOption("price_checker_side", "items", "Add", 0, Item("bronze_sword"), slot = 0)
        player.interfaceOption("price_checker", "items", "Remove-1", 0, Item("bronze_sword"), slot = 0)

        assertEquals(Item("bronze_sword"), player.inventory[0])
        assertTrue(player.offer[0].isEmpty())
    }

    @Test
    fun `Return checked items on interface close`() {
        val player = createPlayer()
        player.inventory.add("bronze_sword")

        player.interfaceOption("worn_equipment", "price", "Show Price-checker")
        player.interfaceOption("price_checker_side", "items", "Add", 0, Item("bronze_sword"), slot = 0)
        player.walk(player.tile.addX(1))
        tick()

        assertEquals(Item("bronze_sword"), player.inventory[0])
        assertTrue(player.offer.isEmpty())
    }
}
