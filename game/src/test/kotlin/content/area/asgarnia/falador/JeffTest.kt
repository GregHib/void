package content.area.asgarnia.falador

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.type.Tile
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JeffTest : WorldTest() {

    @Test
    fun `Talk-to opens Jeff dialogue`() {
        val player = createPlayer(Tile(2978, 3442))
        val jeff = createNPC("jeff", player.tile.addY(1))

        player.npcOption(jeff, "Talk-to")
        tick()

        assertTrue(player.dialogue != null, "the Jeff dialogue opens")
    }

    @Test
    fun `Jeff dialogue can be completed`() {
        val player = createPlayer(Tile(2978, 3442))
        val jeff = createNPC("jeff", player.tile.addY(1))

        player.npcOption(jeff, "Talk-to")
        tick()
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
