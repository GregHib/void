package content.quest.member.rum_deal

import WorldTest
import dialogueContinue
import npcOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Talks to the Captain Braindeath that spawns from braindeath_island.npc-spawns.toml, rather than
 * one created by the test, so the real definition and spawn tile are exercised.
 */
class BraindeathSpawnTest : WorldTest() {

    override var loadNpcs: Boolean = true

    @Test
    fun `The spawned Captain Braindeath talks at the stagnant water stage`() {
        val player = createPlayer(Tile(2144, 5108, 1))
        player["rum_deal"] = "blindweed_added"
        tick(2)

        val braindeath = NPCs.findOrNull(Tile(2144, 5109, 1), "captain_braindeath")
        assertNotNull(braindeath, "Captain Braindeath should be spawned at 2144, 5109, 1")

        player.npcOption(braindeath, "Talk-to")
        tick(3)

        assertNotNull(player.dialogue, "talking to the spawned Braindeath should open a dialogue")

        var steps = 0
        while (player.dialogue != null && steps < 40) {
            player.dialogueContinue()
            steps++
            tick()
        }

        assertEquals("fetch_water", player["rum_deal", "unstarted"], "the conversation should send the player for stagnant water")
        assertTrue(player.inventory.contains("bucket"), "Braindeath hands over a bucket")
    }
}
