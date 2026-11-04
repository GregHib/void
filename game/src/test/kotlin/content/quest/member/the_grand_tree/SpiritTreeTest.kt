package content.quest.member.the_grand_tree

import WorldTest
import interfaceOption
import objectOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpiritTreeTest : WorldTest() {

    @ParameterizedTest
    @ValueSource(strings = ["spirit_tree", "spirit_tree_gnome", "spirit_tree_fullygrown"])
    fun `Grand Tree completion cannot bypass the village quest for spirit tree travel`(id: String) {
        val player = createPlayer(emptyTile)
        player["the_grand_tree"] = "completed"
        val tree = createObject(id, emptyTile.addX(1))
        player.objectOption(tree, "Talk-to")
        tick(3)
        player.skipDialogues()
        assertEquals(null, player.dialogue)
        assertFalse(player.interfaces.contains("spirit_tree"))
        assertEquals(emptyTile, player.tile)
        player.objectOption(tree, "Teleport")
        tick(3)
        player.skipDialogues()
        assertEquals(null, player.dialogue)
        assertFalse(player.interfaces.contains("spirit_tree"))
        assertEquals(emptyTile, player.tile)
        player["tree_gnome_village"] = "completed"
        player.objectOption(tree, "Teleport")
        tick(3)
        assertTrue(player.interfaces.contains("spirit_tree"))
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Stronghold Spirit Tree checks Grand Tree with the missing quest bypass`(bypass: Boolean) {
        val previous = Settings["quests.requirements.skipMissing", false]
        Settings.load(mapOf("quests.requirements.skipMissing" to bypass.toString()))
        try {
            val player = createPlayer(emptyTile)
            val tree = createObject("spirit_tree_stronghold", emptyTile.addX(1))
            player.objectOption(tree, "Talk-to")
            tick(3)
            player.skipDialogues()
            assertEquals(null, player.dialogue)
            assertFalse(player.interfaces.contains("spirit_tree"))
            player.objectOption(tree, "Teleport")
            tick(3)
            player.skipDialogues()
            assertFalse(player.interfaces.contains("spirit_tree"))
            player["tree_gnome_village"] = "completed"
            player.objectOption(tree, "Teleport")
            tick(3)
            assertEquals(bypass, player.interfaces.contains("spirit_tree"))
            if (!bypass) {
                player.skipDialogues()
                player["the_grand_tree"] = "completed"
                player.objectOption(tree, "Teleport")
                tick(3)
                assertTrue(player.interfaces.contains("spirit_tree"))
            }
        } finally {
            Settings.load(mapOf("quests.requirements.skipMissing" to previous.toString()))
        }
    }

    @Test
    fun `Teleport from stronghold tree to grand exchange`() {
        val player = createPlayer(Tile(2461, 3444))
        player["tree_gnome_village"] = "completed"
        player["the_grand_tree"] = "completed"
        val tree = GameObjects.find(Tile(2460, 3445), "spirit_tree_stronghold")
        player.objectOption(tree, "Teleport")
        tick()
        player.interfaceOption("spirit_tree", "text", slot = 2, optionIndex = 0)
        tick(4)
        assertEquals(Tile(3185, 3508), player.tile)
    }
}
