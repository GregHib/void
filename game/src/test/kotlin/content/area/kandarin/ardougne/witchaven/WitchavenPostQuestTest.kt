package content.area.kandarin.ardougne.witchaven

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.type.Tile
import java.util.Properties

class WitchavenPostQuestTest : WorldTest() {

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

    @ParameterizedTest
    @CsvSource(
        "kennith_witchaven,kennith_fishing_platform_invisible,0",
        "kent_witchaven,kent_fishing_platform,1",
        "witchaven_villager_witchaven_4,witchaven_villager_witchaven_4_2,0",
        "witchaven_villager_witchaven_5,witchaven_villager_witchaven_5_2,0",
        "witchaven_villager_witchaven_6,witchaven_villager_witchaven_6_2,0",
        "caroline_witchaven_4,caroline_witchaven_5,1",
    )
    fun `Talk to post-quest Witchaven npcs`(spawn: String, form: String, level: Int) {
        skipMissing(true)
        val player = createPlayer(Tile(2716, 3290, level))
        val npc = createNPC(spawn, Tile(2716, 3291, level))
        assertEquals(form, npc.def(player).stringId)

        player.npcOption(npc, "Talk-to")
        tick(2)
        assertNotNull(player.dialogue)
        player.skipDialogues()
        assertNull(player.dialogue)
    }

    @Test
    fun `Jeb takes you to the fishing platform`() {
        skipMissing(true)
        val player = createPlayer(Tile(2719, 3303))
        val jeb = createNPC("jeb_witchaven", Tile(2719, 3304))
        assertEquals("jeb_witchaven_3", jeb.def(player).stringId)

        player.npcOption(jeb, "Talk-to")
        tick(2)
        player.skipDialogues()
        tick(6)

        assertEquals(Tile(2782, 3273), player.tile)
    }

    @Test
    fun `Jeb takes you back from the fishing platform`() {
        skipMissing(true)
        val player = createPlayer(Tile(2782, 3275))
        val jeb = createNPC("jeb", Tile(2782, 3276))
        assertEquals("jeb_witchaven_3", jeb.def(player).stringId)

        player.npcOption(jeb, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.skipDialogues()
        tick(6)

        assertEquals(Tile(2719, 3301), player.tile)
    }
}
