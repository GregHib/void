package content.area.karamja.tai_bwo_wannai

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.type.Tile
import java.util.Properties

class TaiBwoWannaiNpcsTest : WorldTest() {

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
    fun `Tiadeche offers his karambwan stall`() {
        skipMissing(true)
        val player = createPlayer(Tile(2781, 3056))
        val tiadeche = createNPC("tiadeche_tai_bwo_wannai", Tile(2781, 3057))
        assertEquals("tiadeche_tai_bwo_wannai_2", tiadeche.def(player).stringId)

        player.npcOption(tiadeche, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        tick(1)

        assertTrue(player.hasOpen("shop"))
    }

    @Test
    fun `Tamayu offers his spear stall`() {
        skipMissing(true)
        val player = createPlayer(Tile(2800, 3056))
        val tamayu = createNPC("tamayu_tai_bwo_wannai", Tile(2800, 3057))
        assertEquals("tamayu_karamja_2", tamayu.def(player).stringId)

        player.npcOption(tamayu, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        tick(1)

        assertTrue(player.hasOpen("shop"))
    }

    @Test
    fun `Tinsay greets the player`() {
        skipMissing(true)
        val player = createPlayer(Tile(2790, 3053))
        val tinsay = createNPC("tinsay_tai_bwo_wannai", Tile(2790, 3054))
        assertEquals("tinsay_tai_bwo_wannai_2", tinsay.def(player).stringId)

        player.npcOption(tinsay, "Talk-to")
        tick(1)
        assertNotNull(player.dialogue)
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
