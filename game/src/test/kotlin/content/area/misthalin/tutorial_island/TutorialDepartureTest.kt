package content.area.misthalin.tutorial_island

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class TutorialDepartureTest : WorldTest() {

    private val home get() = Tile(Settings["world.home.x", 0], Settings["world.home.y", 0], Settings["world.home.level", 0])

    @Test
    fun `Welcome message waits until the player has landed in Lumbridge`() {
        val player = createPlayer(Tile(3142, 3088)) { it.startTutorial(67) }
        val instructor = createNPC("magic_instructor", Tile(3141, 3088))

        player.npcOption(instructor, "Talk-to")
        tick(3)
        player.skipDialogues()
        player.dialogueOption(1) // "Yes, please."
        player.skipDialogues() // the player's own line before the instructor acts

        var welcomeTile: Tile? = null
        for (tick in 0 until 20) {
            tick()
            // `statement` opens dialogue_message*; the instructor's own chat is dialogue_npc_chat*.
            if (welcomeTile == null && player.dialogue?.startsWith("dialogue_message") == true) {
                welcomeTile = player.tile
            }
        }

        assertNotNull(welcomeTile, "the welcome message never appeared")
        assertEquals(home, welcomeTile, "the welcome message appeared before the player landed")
        assertEquals(home, player.tile)
    }

    @Test
    fun `Leaving grants the starter kit exactly once`() {
        val player = createPlayer(Tile(3142, 3088)) { it.startTutorial(67) }

        player.leaveTutorial()
        content.entity.player.starterKit(player)

        assertEquals(1, player.inventory.count("bronze_hatchet"))
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
