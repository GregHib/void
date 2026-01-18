package content.quest.member.the_grand_tree

import WorldTest
import interfaceOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpiritTreeTest : WorldTest() {

    @Test
    fun `Can't teleport without quest complete`() {
        val player = createPlayer(Tile(2542, 3169))
        val tree = objects.find(Tile(2543, 3168), "spirit_tree_gnome")
        player.objectOption(tree, "Teleport")
        tick()

        assertNull(player.dialogue)
        assertNull(player.menu)
        assertEquals(Tile(2542, 3169), player.tile)
    }

    @Test
    fun `Teleport from stronghold tree to grand exchange`() {
        val player = createPlayer(Tile(2461, 3444))
        player["the_grand_tree"] = "completed"
        val tree = objects.find(Tile(2460, 3445), "spirit_tree_stronghold")
        player.objectOption(tree, "Teleport")
        tick()
        player.interfaceOption("spirit_tree", "text", slot = 2, optionIndex = 0)
        tick(4)
        assertEquals(Tile(3185, 3508), player.tile)
    }
}
