package content.minigame.pest_control

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

class VoidKnightOutpostNpcsTest : WorldTest() {

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
        "terry_gord_spawn_pest_control,terry_gord_pest_control,2673,2660,0",
        "mrs_gord_spawn_pest_control,mrs_gord_pest_control,2674,2655,0",
        "mariah_spawn_pest_control,mariah_upset_pest_control,2682,2660,0",
        "commodore_spawn_tyr_pest_control,commodore_tyr_pest_control,2683,2671,0",
        "commodore_tyr_vallutas_den,commodore_tyr_vallutas_den_2,4575,5600,0",
    )
    fun `Post-quest void knight outpost npcs talk`(id: String, postQuest: String, x: Int, y: Int, level: Int) {
        skipMissing(true)
        val player = createPlayer(Tile(x, y - 1, level))
        val npc = createNPC(id, Tile(x, y, level))
        assertEquals(postQuest, npc.def(player).stringId)

        player.npcOption(npc, "Talk-to")
        tick(1)
        assertNotNull(player.dialogue)
        player.skipDialogues()

        assertNull(player.dialogue)
    }

    @Test
    fun `Terry Gord asks how he can help before Quiet Before the Swarm`() {
        val player = createPlayer(Tile(2673, 2659))
        val terry = createNPC("terry_gord_spawn_pest_control", Tile(2673, 2660))
        assertEquals("terry_gord_pest_control", terry.def(player).stringId)

        player.npcOption(terry, "Talk-to")
        tick(1)
        player.skipDialogues()
        assertNotNull(player.dialogue)
        player.dialogueOption(3)
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
