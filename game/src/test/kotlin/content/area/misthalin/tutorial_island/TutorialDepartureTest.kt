package content.area.misthalin.tutorial_island

import WorldTest
import dialogueOption
import interfaceOption
import npcOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class TutorialDepartureTest : WorldTest() {

    private val exit get() = Tile(Settings["world.start.tutorial.exit.x", 0], Settings["world.start.tutorial.exit.y", 0], Settings["world.start.tutorial.exit.level", 0])

    @Test
    fun `Terrova sends the player to the final stage rather than teleporting them`() {
        val player = createPlayer(Tile(3142, 3088)) { it.startTutorial(67) }
        val instructor = createNPC("magic_instructor", Tile(3141, 3088))

        player.npcOption(instructor, "Talk-to")
        tick(3)
        player.skipDialogues()
        player.dialogueOption(1) // "Yes."
        player.skipDialogues()
        tick(3)

        assertEquals(TutorialIsland.lastStage, player.tutorialStage)
        assertTrue(player.inTutorial, "the tutorial ends when Home Teleport is cast, not before")
    }

    @Test
    fun `Casting Home Teleport on the final stage ends the tutorial`() {
        val player = createPlayer(Tile(3142, 3088)) { it.startTutorial(TutorialIsland.lastStage) }

        player.interfaceOption("modern_spellbook", "lumbridge_home_teleport", "Cast")
        tick(40)

        assertFalse(player.inTutorial)
        assertTrue(player["tutorial_complete", false])
        assertTrue(player.inventory.contains("bronze_hatchet"), "starter kit missing")
        // The spell does the travelling, landing in the lumbridge_teleport area.
        assertTrue(player.tile.x in 3221..3222 && player.tile.y in 3217..3220, "expected Lumbridge, was ${player.tile}")
    }

    @Test
    fun `Home Teleport stays blocked before the final stage`() {
        val start = Tile(3142, 3088)
        val player = createPlayer(start) { it.startTutorial(66) } // spellbook unlocked, but not the last stage

        player.interfaceOption("modern_spellbook", "lumbridge_home_teleport", "Cast")
        tick(40)

        assertTrue(player.inTutorial)
        assertEquals(start, player.tile)
    }

    @Test
    fun `Skipping through the guide sends the player straight to the exit tile`() {
        settings.setProperty("world.start.tutorial.skippable", "true")
        try {
            val player = createPlayer(Tile(3095, 3107)) { it.startTutorial(0) }
            val guide = createNPC("runescape_guide", Tile(3094, 3107))

            player.npcOption(guide, "Talk-to")
            tick(3)
            player.skipDialogues()
            player.dialogueOption(2) // "No, send me to the mainland."
            player.skipDialogues()
            tick(20)

            assertFalse(player.inTutorial)
            assertEquals(exit, player.tile)
            assertTrue(player.inventory.contains("bronze_hatchet"))
        } finally {
            settings.setProperty("world.start.tutorial.skippable", "false")
        }
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
