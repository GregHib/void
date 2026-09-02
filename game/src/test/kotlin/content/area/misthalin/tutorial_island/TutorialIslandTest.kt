package content.area.misthalin.tutorial_island

import WorldTest
import content.entity.player.modal.Tab
import content.entity.player.modal.gameFrameComponents
import npcOption
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import skipDialogues
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class TutorialIslandTest : WorldTest() {

    private val guideRoom = Tile(3096, 3107)

    /** Skips the character design step so tests start on the island itself. */
    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }

    @Test
    fun `Players outside the tutorial are unaffected`() {
        val player = createPlayer(guideRoom)

        assertFalse(player.inTutorial)
        assertEquals(-1, player.tutorialStage)
        for (component in gameFrameComponents) {
            assertTrue(player.tutorialUnlocked(component), "$component should be unlocked")
        }
    }

    @Test
    fun `Stage only advances from the stage before it`() {
        val player = createPlayer(guideRoom) { it.startTutorial(5) }

        player.advanceTutorial(3)
        assertEquals(5, player.tutorialStage)

        player.advanceTutorial(5)
        assertEquals(6, player.tutorialStage)
    }

    @Test
    fun `Tabs unlock as the stage advances`() {
        val player = createPlayer(guideRoom) { it.startTutorial(0) }

        assertFalse(player.tutorialUnlocked("options"))
        assertFalse(player.tutorialUnlocked("inventory"))
        assertTrue(player.tutorialUnlocked("chat_box"))

        player["tutorial_stage"] = 5
        assertTrue(player.tutorialUnlocked("options"))
        assertTrue(player.tutorialUnlocked("inventory"))
        assertFalse(player.tutorialUnlocked("prayer_list"))
    }

    @Test
    fun `Talking to the guide advances the first stage`() {
        val player = createPlayer(Tile(3095, 3107)) { it.startTutorial(0) }
        val guide = createNPC("runescape_guide", Tile(3094, 3107))

        player.npcOption(guide, "Talk-to")
        tick(3)
        player.skipDialogues()

        assertEquals(1, player.tutorialStage)
    }

    @Test
    fun `Guide room door stays shut until the guide says so`() {
        val player = createPlayer(Tile(3097, 3107)) { it.startTutorial(0) }
        val door = createObject("door_87_closed", Tile(3098, 3107))

        player.objectOption(door, "Open")
        tick()
        assertEquals(0, player.tutorialStage)

        player["tutorial_stage"] = 3
        player.objectOption(door, "Open")
        tick()
        assertEquals(4, player.tutorialStage)
    }

    @Test
    fun `Survival expert hands over the woodcutting kit once`() {
        val player = createPlayer(Tile(3104, 3095)) { it.startTutorial(4) }
        val expert = createNPC("survival_expert", Tile(3103, 3095))

        player.npcOption(expert, "Talk-to")
        tick(3)
        player.skipDialogues()

        assertEquals(5, player.tutorialStage)
        assertTrue(player.inventory.contains("bronze_hatchet"))
        assertTrue(player.inventory.contains("tinderbox"))
    }

    @Test
    fun `Leaving the island grants the starter kit and clears tutorial state`() {
        val player = createPlayer(Tile(3141, 3088)) { it.startTutorial(67) }

        player.leaveTutorial()

        assertFalse(player.inTutorial)
        assertTrue(player["tutorial_complete", false])
        // `Introduction` keys off `creation`, so leaving must stamp it or the starter kit
        // would be handed out a second time on the next login.
        assertTrue(player["creation", 0L] > 0L)
        for (component in gameFrameComponents) {
            assertTrue(player.tutorialUnlocked(component), "$component should be unlocked")
        }
    }

    @Test
    fun `Progress keeps updating after the overlay is already open`() {
        val player = createPlayer(guideRoom) { it.startTutorial(0) }

        player.renderTutorial()
        assertTrue(player.hasOpen("tutorial_overlay"))
        assertTrue(player.hasOpen("tutorial_text"))

        // `open` returns false for an already-open interface, so a render that guards on it
        // would silently stop updating here.
        player["tutorial_stage"] = 40
        player.renderTutorial()

        // round(40 / 68 * 20) = 12 segments, and the varp is one more than the segment count.
        assertEquals(13, player["tutorial_progress", -1])
    }

    @Test
    fun `Burning the bread still finishes the cooking stage`() {
        val player = createPlayer(Tile(3076, 3081)) { it.startTutorial(17) }

        player.inventory.add("burnt_bread")
        tick()

        assertEquals(18, player.tutorialStage)
    }

    @Test
    fun `The chef replaces ingredients lost to a burnt loaf`() {
        val player = createPlayer(Tile(3075, 3086)) { it.startTutorial(17) }
        val chef = createNPC("master_chef", Tile(3075, 3085))

        player.npcOption(chef, "Talk-to")
        tick(3)
        player.skipDialogues()

        assertTrue(player.inventory.contains("pot_of_flour"))
        assertTrue(player.inventory.contains("bucket_of_water"))
        assertEquals(17, player.tutorialStage)
    }

    @TestFactory
    fun `Every stage row is renderable`(): List<DynamicTest> = (0 until TutorialIsland.stages).map { stage ->
        DynamicTest.dynamicTest("stage $stage") {
            val row = TutorialIsland.row(stage)
            assertTrue(row != null, "missing row for stage $stage")
            val lines = row!!.stringListOrNull("lines") ?: emptyList()
            assertTrue(lines.size <= TUTORIAL_TEXT_LINES, "stage $stage has ${lines.size} lines, the box fits $TUTORIAL_TEXT_LINES")
            val flash = row.stringOrNull("flash")
            if (flash != null) {
                assertTrue(flash == "RunOrb" || Tab.entries.any { it.name == flash }, "unknown flash target $flash")
            }
            val unlock = row.stringOrNull("unlock")
            if (unlock != null) {
                assertTrue(gameFrameComponents.contains(unlock), "unknown unlock target $unlock")
            }
            val npc = row.stringOrNull("hint_npc")
            if (npc != null) {
                assertTrue(NPCDefinitions.ids.containsKey(npc), "unknown hint npc $npc")
            }
        }
    }

    @Test
    fun `Stage table covers every stage index`() {
        val rows = Tables.get(TutorialIsland.TABLE).rows()
        assertEquals(rows.size, TutorialIsland.stages)
        for (stage in 0 until TutorialIsland.stages) {
            assertTrue(TutorialIsland.row(stage) != null, "missing stage $stage")
        }
    }
}
