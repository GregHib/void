package content.skill.summoning

import WorldTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.type.Tile

class BeastOfBurdenCompactTest : WorldTest() {

    @Test
    fun `opening familiar inventory reorganises items to the top`() {
        val player = createPlayer(Tile(3200, 3200))
        player.summonFamiliar(NPCDefinitions.get("pack_yak_familiar"), false)
        tick(3)

        // Stored items scattered with gaps between them.
        player.beastOfBurden.set(3, "bronze_sword", 1)
        player.beastOfBurden.set(10, "coins", 500)

        player.openBeastOfBurden()

        // Compacted to the top, original order preserved, gaps pushed to the bottom.
        assertEquals("bronze_sword", player.beastOfBurden.items[0].id)
        assertEquals("coins", player.beastOfBurden.items[1].id)
        assertEquals(500, player.beastOfBurden.items[1].amount)
        assertEquals(2, player.beastOfBurden.items.indexOfFirst { it.isEmpty() })
    }
}
