package content.skill.summoning

import WorldTest
import containsMessage
import interfaceOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class BeastOfBurdenCapacityTest : WorldTest() {

    /**
     * Regression: a familiar must never store more items than its slot capacity,
     * even when storing many of the same non-stackable item.
     */
    @Test
    fun `cannot store more non-stackable items than capacity`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("thorny_snail_familiar"), false)
        tick(3)
        assertEquals(3, player.beastOfBurdenCapacity)

        player.inventory.add("bronze_sword", 5)
        player.openBeastOfBurden()
        player.interfaceOption("summoning_side", "inventory", "Store-All", item = Item("bronze_sword"), slot = 0)

        assertEquals(3, player.beastOfBurden.count("bronze_sword"))
        assertEquals(2, player.inventory.count("bronze_sword"))
        assertTrue(player.containsMessage("Your familiar can't carry any more items."))
    }

    @Test
    fun `war tortoise stores up to eighteen slots`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("war_tortoise_familiar"), false)
        tick(3)
        assertEquals(18, player.beastOfBurdenCapacity)

        player.inventory.add("bronze_sword", 28)
        player.openBeastOfBurden()
        player.interfaceOption("summoning_side", "inventory", "Store-All", item = Item("bronze_sword"), slot = 0)

        assertEquals(18, player.beastOfBurden.count("bronze_sword"))
        assertEquals(10, player.inventory.count("bronze_sword"))
    }

    @Test
    fun `stackable items only occupy a single slot`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("thorny_snail_familiar"), false)
        tick(3)

        // Fill two of three slots with distinct non-stackable items.
        player.beastOfBurden.add("bronze_dagger", 1)
        player.beastOfBurden.add("iron_dagger", 1)
        player.inventory.add("coins", 1000)
        player.openBeastOfBurden()

        player.interfaceOption("summoning_side", "inventory", "Store-All", item = Item("coins"), slot = 0)

        // Coins fit in the single remaining slot regardless of amount.
        assertEquals(1000, player.beastOfBurden.count("coins"))
        assertEquals(0, player.inventory.count("coins"))
    }
}
