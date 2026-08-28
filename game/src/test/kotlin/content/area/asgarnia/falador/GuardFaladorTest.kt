package content.area.asgarnia.falador

import WorldTest
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.type.Tile
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GuardFaladorTest : WorldTest() {

    @Test
    fun `Talk-to opens the brigand lookout dialogue`() {
        val player = createPlayer(Tile(2967, 3451))
        val guard = createNPC("guard_falador_7", player.tile.addY(1))

        player.npcOption(guard, "Talk-to")
        tick()

        assertTrue(player.dialogue != null, "the guard dialogue opens")
    }

    @Test
    fun `Guard dialogue can be completed`() {
        val player = createPlayer(Tile(2967, 3451))
        val guard = createNPC("guard_falador_7", player.tile.addY(1))

        player.npcOption(guard, "Talk-to")
        tick()
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
