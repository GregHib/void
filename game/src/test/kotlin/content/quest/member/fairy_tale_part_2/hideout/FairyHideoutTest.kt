package content.quest.member.fairy_tale_part_2.hideout

import WorldTest
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.type.Tile
import java.util.Properties

class FairyHideoutTest : WorldTest() {

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
        "fairy_fairy_hq,fairy_9_2,1565,4258",
        "fairy_fairy_hq_2,fairy_fairy_hq_2_2,1572,4261",
        "fairy_fairy_hq_3,fairy_fairy_hq_3_2,1578,4258",
        "fairy_fairy_hq_4,fairy_fairy_hq_4_2,1565,4265",
        "fairy_fairy_hq_5,fairy_fairy_hq_5_2,1566,4260",
        "fairy_nuff_fairy_hq_2,fairy_nuff_2,2281,4456",
        "fairy_very_wise_fairy_hq_2,fairy_very_wise_3,1585,4254",
        "fairy_queen_fairy_hq_2,fairy_queen_2_3,1584,4265",
    )
    fun `Post-quest fairies talk`(id: String, postQuest: String, x: Int, y: Int) {
        skipMissing(true)
        val player = createPlayer(Tile(x, y - 1))
        val npc = createNPC(id, Tile(x, y))
        assertEquals(postQuest, npc.def(player).stringId)

        player.npcOption(npc, "Talk-to")
        tick(1)
        assertNotNull(player.dialogue)
        player.skipDialogues()

        assertNull(player.dialogue)
    }
}
