package content.activity.event.random

import WorldTest
import content.quest.instanceOffset
import interfaceOption
import kotlinx.coroutines.test.runTest
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.MoveInventoryItem
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BeekeeperTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val field = Tile(1931, 5044)

    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "beekeeper")
        tick(8)
        return player
    }

    private fun Player.keeper(): NPC? = (-2..2).flatMap { dx -> (-2..2).map { dy -> tile.add(dx, dy) } }
        .firstNotNullOfOrNull { t -> NPCs.firstOrNull(t) { it.id == "bee_keeper" && it.owner == this } }

    private fun Player.talkThroughIntro() {
        npcOption(keeper()!!, "Talk-To")
        tick(2)
        skipDialogues()
        tick()
    }

    private fun Player.drag(from: Int, to: Int) = runTest {
        instructions.send(MoveInventoryItem(420, from, -1, 0, 420, to, -1, 0))
    }

    private fun Player.placeParts(correct: Boolean) {
        for (source in 1..4) {
            val part = get("beekeeper_part_$source", 0)
            val destination = if (correct) part else (part % 4) + 1
            drag(11 + source, 15 + destination)
            tick()
        }
    }

    private fun Player.build() {
        interfaceOption("beehive_build", "build", "Build")
        tick(2)
        skipDialogues()
        tick()
    }

    @Test
    fun `The Bee keeper drags the player to a private copy of his field`() {
        val player = enter("bees_start")

        assertEquals("beekeeper", player.get<String>("random_event"))
        assertEquals(field, player.tile.minus(player.instanceOffset()))
        assertTrue(player.contains("instance_offset"))
        assertEquals(6, player.get("beekeeper_tries", 0))
        assertEquals(listOf(1, 2, 3, 4), (1..4).map { player.get("beekeeper_part_$it", 0) }.sorted())
    }

    @Test
    fun `Building with missing components is rejected`() {
        val player = enter("bees_missing")
        player.talkThroughIntro()

        // No skipDialogues: the rejection is a no-prompt statement that stays open.
        player.interfaceOption("beehive_build", "build", "Build")
        tick(2)

        assertEquals(6, player.get("beekeeper_tries", 0))
        assertEquals("beekeeper", player.get<String>("random_event"))
    }

    @Test
    fun `Dragging a part moves it into the frame`() {
        val player = enter("bees_drag")
        player.talkThroughIntro()

        val part = player.get("beekeeper_part_1", 0)
        player.drag(12, 16)
        tick()

        assertEquals(0, player.get("beekeeper_part_1", 0))
        assertEquals(part, player.get("beekeeper_slot_1", 0))
    }

    @Test
    fun `The correct order builds the hive for a gift`() {
        val player = enter("bees_win")
        player.talkThroughIntro()

        player.placeParts(correct = true)
        player.build()
        tick(2)

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `A wrong order costs a try`() {
        val player = enter("bees_wrong")
        player.talkThroughIntro()

        player.placeParts(correct = false)
        player.build()

        assertEquals(5, player.get("beekeeper_tries", 0))
        assertEquals("beekeeper", player.get<String>("random_event"))
    }

    @Test
    fun `Six wrong builds send the player home empty-handed`() {
        val player = enter("bees_fail")
        player.talkThroughIntro()

        player.placeParts(correct = false)
        repeat(6) {
            player.build()
        }
        tick(4)

        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
        assertFalse(player.inventory.contains("random_event_gift"))
    }
}
