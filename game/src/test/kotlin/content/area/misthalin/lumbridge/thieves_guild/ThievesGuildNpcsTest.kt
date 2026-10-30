package content.area.misthalin.lumbridge.thieves_guild

import WorldTest
import dialogueOption
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

class ThievesGuildNpcsTest : WorldTest() {

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
    fun `Darren Lightfinger talks about the guild after Buyers and Cellars`() {
        skipMissing(true)
        val player = createPlayer(Tile(4662, 5902))
        val darren = createNPC("guildmaster_darren", Tile(4662, 5903))
        assertEquals("guildmaster_darren_3", darren.def(player).stringId)

        player.npcOption(darren, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        assertNotNull(player.dialogue)
        player.dialogueOption(2)
        player.skipDialogues()

        assertNull(player.dialogue)
    }

    @Test
    fun `Chief Thief Robin talks after Buyers and Cellars`() {
        skipMissing(true)
        val player = createPlayer(Tile(4663, 5903))
        val robin = createNPC("chief_thief_robin", Tile(4663, 5904))
        assertEquals("chief_thief_robin_2", robin.def(player).stringId)

        player.npcOption(robin, "Talk-to")
        tick(1)
        player.skipDialogues()
        player.dialogueOption(3)
        player.skipDialogues()
        assertNotNull(player.dialogue)
        player.dialogueOption(5)
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
