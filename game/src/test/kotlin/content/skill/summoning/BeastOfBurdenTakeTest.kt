package content.skill.summoning

import WorldTest
import containsMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class BeastOfBurdenTakeTest : WorldTest() {

    /**
     * Regression: Take BoB must withdraw as many items as fit when the inventory
     * has fewer free slots than the familiar is carrying, rather than failing
     * all-or-nothing and withdrawing nothing.
     */
    @Test
    fun `take bob fills inventory and leaves the remainder`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)

        // Two free inventory slots, five swords carried.
        player.inventory.add("bronze_sword", 26)
        player.beastOfBurden.add("bronze_sword", 5)

        player.takeAllBeastOfBurden()

        assertEquals(28, player.inventory.count("bronze_sword"))
        assertEquals(3, player.beastOfBurden.count("bronze_sword"))
        assertTrue(player.containsMessage("You don't have enough inventory space."))
    }

    @Test
    fun `take bob withdraws everything when there is room`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)

        player.beastOfBurden.add("bronze_sword", 5)

        player.takeAllBeastOfBurden()

        assertEquals(5, player.inventory.count("bronze_sword"))
        assertEquals(0, player.beastOfBurden.count("bronze_sword"))
        assertFalse(player.containsMessage("You don't have enough inventory space."))
    }
}
