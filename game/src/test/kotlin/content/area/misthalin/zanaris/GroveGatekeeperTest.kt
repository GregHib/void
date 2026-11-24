package content.area.misthalin.zanaris

import WorldTest
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.type.Tile
import java.util.Properties

class GroveGatekeeperTest : WorldTest() {

    private fun skipMissing(skip: Boolean) {
        val properties = Properties()
        properties.putAll(settings)
        properties["quests.requirements.skipMissing"] = skip.toString()
        Settings.load(properties)
        SettingsReload.now()
    }

    @AfterEach
    fun reset() {
        skipMissing(false)
    }

    @Test
    fun `Gatekeeper by the grove talks after Fairy Tale I`() {
        skipMissing(true)
        val player = createPlayer(Tile(2401, 4377))
        val gatekeeper = createNPC("gatekeeper_2", Tile(2401, 4378))
        assertEquals("gatekeeper_2_2", gatekeeper.def(player).stringId)

        player.npcOption(gatekeeper, "Talk-to")
        tick(1)
        assertNotNull(player.dialogue)
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
