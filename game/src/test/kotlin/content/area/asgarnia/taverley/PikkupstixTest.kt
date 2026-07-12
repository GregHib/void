package content.area.asgarnia.taverley

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.type.Tile
import kotlin.test.assertTrue

class PikkupstixTest : WorldTest() {

    @Test
    fun `Talk-to opens the summoning tutor menu`() {
        val player = createPlayer(Tile(2523, 3056))
        val pikkupstix = createNPC("pikkupstix", player.tile.addY(1))

        player.npcOption(pikkupstix, "Talk-to")
        tick(1)

        assertTrue(player.dialogue != null, "the tutor dialogue opens")
    }

    @Test
    fun `Enchant explains how to fill headgear with scrolls`() {
        val player = createPlayer(Tile(2523, 3056))
        val pikkupstix = createNPC("pikkupstix", player.tile.addY(1))

        player.npcOption(pikkupstix, "Enchant")
        tick(1)

        assertTrue(player.dialogue != null, "he explains what to do")
    }
}
