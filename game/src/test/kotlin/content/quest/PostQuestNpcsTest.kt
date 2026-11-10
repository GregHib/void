package content.quest

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class PostQuestNpcsTest : WorldTest() {

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
        "merlin_camelot_castle,merlin_normal,2767,3499,1",
        "observatory_professor_observatory_2,observatory_professor_observatory_3,2437,3160,1",
        "guard_sinclair_mansion,guard_sinclair_mansion_2,2739,3577,0",
        "anna,anna_2,2734,3575,0",
        "bob_sinclair_mansion,bob_sinclair_mansion_2,2748,3559,0",
        "carol,carol_2,2734,3581,1",
        "david,david_2,2739,3581,0",
        "elizabeth,elizabeth_2,2746,3581,1",
        "frank,frank_2,2742,3577,0",
        "ned_crandor,ned_port_sarim_2,2849,3235,1",
        "drunken_sailor_2,drunken_sailor_normal_2,3055,3254,0",
        "dimintheis,dimintheis_normal,3279,3404,0",
        "radimus_erkle_2,radimus_erkle_3,2724,3377,0",
        "kathy_corkat,kathy_corkat_piscatoris,2368,3486,0",
        "elkoy_tree_gnome_village,elkoy_tree_gnome_village_4,2504,3191,0",
    )
    fun `Talk to post-quest npcs`(spawn: String, form: String, x: Int, y: Int, level: Int) {
        skipMissing(true)
        val npc = createNPC(spawn, Tile(x, y, level))
        val player = createPlayer(npc.tile.addY(-1))
        assertEquals(form, npc.def(player).stringId)

        player.npcOption(npc, "Talk-to")
        tick(2)
        assertNotNull(player.dialogue)
    }

    @Test
    fun `Elkoy leads you into the village`() {
        skipMissing(true)
        val player = createPlayer(Tile(2504, 3192))
        val elkoy = createNPC("elkoy_tree_gnome_village", Tile(2504, 3191))

        player.npcOption(elkoy, "Follow")
        tick(5)

        assertEquals(Tile(2515, 3159), player.tile)
    }

    @Test
    fun `Elkoy leads you out of the village`() {
        skipMissing(true)
        val player = createPlayer(Tile(2515, 3159))
        val elkoy = createNPC("elkoy_tree_gnome_village_2", Tile(2514, 3159))

        player.npcOption(elkoy, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(1)
        player.dialogueContinue(2)
        tick(5)

        assertEquals(Tile(2504, 3192), player.tile)
    }

    @Test
    fun `Kathy rows you north to the fishing colony`() {
        skipMissing(true)
        val player = createPlayer(Tile(2368, 3485))
        val kathy = createNPC("kathy_corkat", Tile(2368, 3486))

        player.npcOption(kathy, "Travel")
        tick(10)

        assertEquals(Tile(2357, 3641), player.tile)
    }

    @Test
    fun `Kathy rows you back up the river`() {
        val player = createPlayer(Tile(2357, 3641))
        val kathy = createNPC("kathy_corkat_piscatoris", Tile(2357, 3640))

        player.npcOption(kathy, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2)
        player.dialogueContinue(1)
        tick(10)

        assertEquals(Tile(2369, 3484), player.tile)
    }

    @Test
    fun `Search the drunken sailor for his hat`() {
        skipMissing(true)
        val player = createPlayer(Tile(3055, 3253))
        val sailor = createNPC("drunken_sailor_2", Tile(3055, 3254))

        player.npcOption(sailor, "Search")
        tick(2)
        player.skipDialogues()
        assertTrue(player.inventory.contains("sailors_hat"))

        player.npcOption(sailor, "Search")
        tick(2)
        player.skipDialogues()
        assertEquals(1, player.inventory.count("sailors_hat"))
    }

    @Test
    fun `Observatory professor hands out a chart`() {
        skipMissing(true)
        val player = createPlayer(Tile(2437, 3159, 1))
        val professor = createNPC("observatory_professor_observatory_2", Tile(2437, 3160, 1))
        player.inventory.add("sextant")
        player.inventory.add("watch")

        player.npcOption(professor, "Talk-to")
        tick(2)
        player.skipDialogues()
        player.dialogueOption(2)
        player.skipDialogues()

        assertTrue(player.inventory.contains("chart"))
        assertNull(player.dialogue)
    }
}
