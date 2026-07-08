package content.activity.event.random

import WorldTest
import dialogueOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CerterTest : WorldTest() {

    private val spot = Tile(3221, 3218)

    private fun setup(name: String): Pair<Player, NPC> {
        val player = createPlayer(spot, name)
        player["random_event"] = "certer"
        player["random_event_origin"] = spot.id
        val giles = createNPC("giles", spot.addX(1))
        giles["owner"] = player.accountName
        return player to giles
    }

    private fun openPuzzle(player: Player, giles: NPC) {
        player.npcOption(giles, "Talk-to")
        tick()
        player.skipDialogues() // greeting + item box -> stops on the choice menu
        tick()
    }

    @Test
    fun `Event spawns a certer beside the player`() {
        val player = createPlayer(spot, "certer_spawn")
        RandomEvents.start(player, "certer")
        tick(2)

        val giles = (-2..2).flatMap { dx -> (-2..2).map { dy -> player.tile.add(dx, dy) } }
            .firstNotNullOfOrNull { t -> NPCs.firstOrNull(t) { it.id == "giles" } }
        assertTrue(giles != null, "Expected a certer spawned near the player")
    }

    @Test
    fun `Correctly identifying the item rewards loot and ends the event`() {
        val (player, giles) = setup("certer_correct")
        openPuzzle(player, giles)

        val answer = player.get("certer_answer", 0)
        player.dialogueOption(answer)
        tick()
        player.skipDialogues() // "Thank you, I hope you like your present..."
        tick()

        assertFalse(player.inventory.isEmpty(), "Expected the certer reward")
        assertNull(player.get<String>("random_event"))
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `A wrong answer ends the event with no reward`() {
        val (player, giles) = setup("certer_wrong")
        openPuzzle(player, giles)

        val answer = player.get("certer_answer", 0)
        val wrong = if (answer == 1) 2 else 1
        player.dialogueOption(wrong)
        tick()
        player.skipDialogues() // "Sorry, I don't think so."
        tick()

        assertTrue(player.inventory.isEmpty(), "Expected no reward for a wrong answer")
        assertNull(player.get<String>("random_event"))
        assertTrue(player.contains("random_event_cooldown"))
    }
}
