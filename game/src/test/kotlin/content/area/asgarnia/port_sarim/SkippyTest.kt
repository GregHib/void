package content.area.asgarnia.port_sarim

import WorldTest
import dialogueContinue
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SkippyTest : WorldTest() {

    @Test
    fun `Talk-To opens Skippy dialogue`() {
        val player = createPlayer(Tile(2983, 3195))
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Talk-To")
        tick()

        assertTrue(player.dialogue != null, "the Skippy dialogue opens")
    }

    @Test
    fun `Talk-To still opens dialogue after Skippy asks for water`() {
        val player = createPlayer(Tile(2983, 3195)) {
            it["skippy_state"] = "need_bucket_of_water"
        }
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Talk-To")
        tick()

        assertTrue(player.dialogue != null, "Talk-To should still open Skippy's main dialogue")
    }

    @Test
    fun `Sober-Up opens reminder after Skippy asks for water`() {
        val player = createPlayer(Tile(2983, 3195)) {
            it["skippy_state"] = "need_bucket_of_water"
        }
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Sober-Up")
        tick()

        assertTrue(player.dialogue != null, "Sober-Up should open the water reminder dialogue")
    }

    @Test
    fun `Sober-Up opens reminder without requiring Skippy state`() {
        val player = createPlayer(Tile(2983, 3195))
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Sober-Up")
        tick()

        assertTrue(player.dialogue != null, "Sober-Up should open the water reminder dialogue without relying on transient state")
    }

    @Test
    fun `Choosing the mudskipper branch unlocks Sober-Up`() {
        val player = createPlayer(Tile(2983, 3195))
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Talk-To")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption("line2")
        player.skipDialogues()

        assertEquals("need_bucket_of_water", player["skippy_state", "unstarted"])

        player.npcOption(skippy, "Sober-Up")
        tick()

        assertTrue(player.dialogue != null, "Sober-Up should work after the main branch sets Skippy's state")
    }

    @Test
    fun `Skippy state is set before the final dialogue closes`() {
        val player = createPlayer(Tile(2983, 3195))
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Talk-To")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption("line2")
        var stateSetWhileOpen =
            player["skippy_state", "unstarted"] == "need_bucket_of_water" &&
                player.dialogue != null
        while (player.dialogue != null && player["skippy_state", "unstarted"] == "unstarted") {
            player.dialogueContinue()
            if (player["skippy_state", "unstarted"] == "need_bucket_of_water" && player.dialogue != null) {
                stateSetWhileOpen = true
            }
        }

        assertEquals("need_bucket_of_water", player["skippy_state", "unstarted"])
        assertTrue(stateSetWhileOpen, "Skippy's state should be set before the final dialogue closes")
    }

    @Test
    fun `Selecting the mudskipper option sets Skippy state immediately`() {
        val player = createPlayer(Tile(2983, 3195))
        val skippy = createNPC("skippy", Tile(2983, 3196))

        player.npcOption(skippy, "Talk-To")
        tick()
        player.dialogueContinue(4)
        player.dialogueOption("line2")

        assertEquals("need_bucket_of_water", player["skippy_state", "unstarted"])
    }
}
