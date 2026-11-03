package content.area.kandarin

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.SettingsReload
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import java.util.Properties

class KandarinPostQuestTest : WorldTest() {

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
    fun `Chuck mentions Jim then talks about penguin spies`() {
        skipMissing(true)
        val player = createPlayer(Tile(2615, 3258))
        val chuck = createNPC("chuck_monastery", Tile(2615, 3259))

        assertEquals("chuck_monastery_3", chuck.def(player).stringId)
        player.npcOption(chuck, "Talk-to")
        tick(2)
        player.skipDialogues()

        assertTrue(player["chuck_iceberg_agent_mentioned", false])

        player.dialogueOption(1) // Penguin Hide and Seek
        player.dialogueContinue()

        assertEquals("dialogue_multi4", player.dialogue)
    }

    @Test
    fun `Chuck replaces a lost clockwork book`() {
        skipMissing(true)
        val player = createPlayer(Tile(2615, 3258))
        val chuck = createNPC("chuck_monastery", Tile(2615, 3259))
        player["chuck_iceberg_agent_mentioned"] = true

        player.npcOption(chuck, "Talk-to")
        tick(2)
        player.dialogueContinue(2)
        player.dialogueOption(2) // I've lost the clockwork book, could I have another?
        player.dialogueContinue()

        assertEquals(1, player.inventory.count("clockwork_book"))
    }

    @Test
    fun `Chuck pays out penguin points`() {
        skipMissing(true)
        val player = createPlayer(Tile(2615, 3258))
        val chuck = createNPC("chuck_monastery", Tile(2615, 3259))
        player["penguin_points"] = 2

        player.npcOption(chuck, "Hide-n-Seek")
        tick(2)
        player.dialogueContinue(2)
        player.dialogueOption(1) // Show me the money!
        player.dialogueContinue()

        assertEquals(13_000, player.inventory.count("coins"))
        assertEquals(0, player["penguin_points", 0])
    }

    @Test
    fun `Scotty and Sanchez talk about cleaning`() {
        skipMissing(true)
        val player = createPlayer(Tile(2036, 4630))
        val scotty = createNPC("scotty_temple_of_light", Tile(2037, 4630))
        val sanchez = createNPC("sanchez_temple_of_light", Tile(2035, 4630))

        assertEquals("scotty_temple_of_light_2", scotty.def(player).stringId)
        assertEquals("sanchez_temple_of_light_2", sanchez.def(player).stringId)
        player.npcOption(scotty, "Talk-to")
        tick(2)

        assertNotNull(player.dialogue)
        player.skipDialogues()

        player.npcOption(sanchez, "Talk-to")
        tick(2)

        assertNotNull(player.dialogue)
    }

    @Test
    fun `Nickolaus talks about the eagles`() {
        skipMissing(true)
        val player = createPlayer(Tile(2318, 3502))
        val nickolaus = createNPC("nickolaus_2", Tile(2318, 3503))

        assertEquals("nickolaus_4", nickolaus.def(player).stringId)
        player.npcOption(nickolaus, "Talk-to")
        tick(2)
        player.dialogueContinue(2)

        assertEquals("dialogue_multi2", player.dialogue)
    }
}
