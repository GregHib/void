package content.skill.farming

import WorldTest
import content.entity.player.bank.bank
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals

class FarmingSaplingsTest : WorldTest() {

    @Test
    fun `Unwatered seedlings don't get replaced`() {
        val player = createPlayer()
        player.inventory.add("oak_seedling")
        player.inventory.add("willow_seedling")

        val farming = scripts.filterIsInstance<Farming>().first()
        farming.grow(player, 5)

        assertEquals("oak_seedling", player.inventory[0].id)
        assertEquals("willow_seedling", player.inventory[1].id)
    }

    @Test
    fun `Watered seedlings grow into saplings`() {
        val player = createPlayer()
        player.inventory.add("willow_seedling_w")
        player.inventory.add("maple_seedling_w")
        player.inventory.add("willow_seedling_w")

        val farming = scripts.filterIsInstance<Farming>().first()
        farming.grow(player, 5)

        assertEquals("willow_sapling", player.inventory[0].id)
        assertEquals("maple_sapling", player.inventory[1].id)
        assertEquals("willow_sapling", player.inventory[2].id)
    }

    @Test
    fun `Banked seedlings get replaced with saplings`() {
        val player = createPlayer()
        player.bank.add("willow_seedling_w")
        player.bank.add("maple_seedling_w", 3)

        val farming = scripts.filterIsInstance<Farming>().first()
        farming.grow(player, 5)

        assertEquals(Item("willow_sapling"), player.bank[0])
        assertEquals(Item("maple_sapling", 3), player.bank[1])
    }

    @Test
    fun `Banked seedlings get stacked with saplings`() {
        val player = createPlayer()
        player.bank.add("oak_logs")
        player.bank.add("willow_sapling", 2)
        player.bank.add("willow_seedling_w", 2)
        player.bank.add("willow_logs")
        player.bank.add("maple_sapling", 2)
        player.bank.add("maple_seedling_w")
        player.bank.add("maple_logs")

        val farming = scripts.filterIsInstance<Farming>().first()
        farming.grow(player, 5)

        assertEquals(Item("willow_sapling", 4), player.bank[1])
        assertEquals(Item("maple_sapling", 3), player.bank[3])
    }
}
