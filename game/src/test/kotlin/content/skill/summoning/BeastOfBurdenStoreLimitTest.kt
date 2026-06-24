package content.skill.summoning

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class BeastOfBurdenStoreLimitTest : WorldTest() {

    /**
     * Regression: storing more than is carried must not trigger moveToLimit's
     * undo path, which previously re-shuffled the already-stored items and left
     * gaps in the beast of burden (the next deposit then "skipped" slots).
     */
    @Test
    fun `storing more than held does not leave gaps`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.openBeastOfBurden()

        // Three swords already stored (non-stackable -> three slots).
        player.beastOfBurden.add("bronze_sword", 3)
        // Five swords held, but request ten.
        player.inventory.add("bronze_sword", 5)

        player.interfaceOption("summoning_side", "inventory", "Store-10", item = Item("bronze_sword"), slot = 0)

        assertEquals(8, player.beastOfBurden.count("bronze_sword"))
        assertEquals(0, player.inventory.count("bronze_sword"))
        // All eight swords sit in contiguous slots 0..7 with no gaps.
        assertEquals(8, player.beastOfBurden.items.indexOfFirst { it.isEmpty() })
    }

    @Test
    fun `withdrawing more than carried does not leave gaps`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)
        player.openBeastOfBurden()

        player.inventory.add("bronze_sword", 3)
        player.beastOfBurden.add("bronze_sword", 5)

        player.interfaceOption("beast_of_burden", "items", "Withdraw-10", item = Item("bronze_sword"), slot = 0)

        assertEquals(8, player.inventory.count("bronze_sword"))
        assertEquals(0, player.beastOfBurden.count("bronze_sword"))
        assertEquals(8, player.inventory.items.indexOfFirst { it.isEmpty() })
    }
}
