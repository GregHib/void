package content.quest

import WorldTest
import objectOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.type.Tile
import java.util.Properties

class SkippedQuestsTest : WorldTest() {

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
    fun `Npcs show their post-quest form for skipped quests`() {
        skipMissing(true)
        val player = createPlayer(Tile(3359, 2994))
        val sumona = createNPC("sumona", Tile(3358, 2994))

        assertEquals("sumona_2", sumona.def(player).stringId)
        assertEquals("unstarted", player.quest("icthlarins_little_helper"))
    }

    @Test
    fun `Npcs keep their pre-quest form when not skipping`() {
        skipMissing(false)
        val player = createPlayer(Tile(3359, 2994))
        val lokar = createNPC("lokar_searunner", Tile(2621, 3688))

        assertEquals("lokar_searunner_2", lokar.def(player).stringId)
    }

    @Test
    fun `Saved quest progress overrides the skipped default`() {
        skipMissing(true)
        val player = createPlayer(Tile(2621, 3687))
        val lokar = createNPC("lokar_searunner", Tile(2621, 3688))
        player["lunar_diplomacy"] = "unstarted"

        assertEquals("lokar_searunner_2", lokar.def(player).stringId)
    }

    @Test
    fun `Culinaromancer's chest opens the bank`() {
        skipMissing(true)
        val player = createPlayer(Tile(3219, 9622))
        val chest = createObject("culinaromancers_chest_base", Tile(3219, 9623))

        assertEquals("culinaromancers_chest", chest.def(player).stringId)
        player.objectOption(chest, "Bank")
        tick(5)

        assertTrue(player.hasOpen("bank"))
    }
}
