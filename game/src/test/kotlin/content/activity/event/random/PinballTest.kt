package content.activity.event.random

import WorldTest
import content.quest.instance
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PinballTest : WorldTest() {

    private val origin = Tile(3221, 3218)

    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "pinball")
        tick(8)
        while (player.dialogue != null) {
            player.skipDialogues()
            tick()
        }
        tick(6) // the old man waves, puffs away and the first post lights up
        return player
    }

    private fun Player.tag(post: String) {
        val obj = createObject(post, tile.addY(1))
        objectOption(obj, "Tag")
        tick(4)
    }

    @Test
    fun `The old man kidnaps the player to a private pinball arena`() {
        val player = enter("pb_start")

        assertEquals("pinball", player.get<String>("random_event"))
        assertNotNull(player.instance())
        assertTrue(player.interfaces.contains("pinball_overlay"))
        assertEquals(1, player.get("pinball_target", 0)) // deterministic first post in tests
        assertEquals(0, player.get("pinball_score", 0))
    }

    @Test
    fun `Tagging the flashing post scores a point and lights the next`() {
        val player = enter("pb_score")

        player.tag("pinball_post_1")

        assertEquals(1, player.get("pinball_score", 0))
        assertEquals(1, player.get("pinball_target", 0))
    }

    @Test
    fun `Tagging an unlit post resets the score`() {
        val player = enter("pb_reset")
        player.tag("pinball_post_1")
        assertEquals(1, player.get("pinball_score", 0))

        player.tag("pinball_post_2")

        assertEquals(0, player.get("pinball_score", 0))
        assertEquals("pinball", player.get<String>("random_event"))
    }

    @Test
    fun `The trolls block the exit until ten points`() {
        val player = enter("pb_blocked")
        val exit = createObject("pinball_cave_exit", player.tile.addY(1))

        player.objectOption(exit, "Exit")
        tick(2)
        while (player.dialogue != null) {
            player.skipDialogues()
            tick()
        }

        assertEquals("pinball", player.get<String>("random_event"))
        assertNotNull(player.instance())
        assertTrue(player.inventory.isEmpty())
    }

    @Test
    fun `Ten points opens the way out for a gift`() {
        val player = enter("pb_win")

        repeat(10) {
            player.tag("pinball_post_1")
        }
        assertEquals(10, player.get("pinball_score", 0))

        val exit = createObject("pinball_cave_exit", player.tile.addY(1))
        player.objectOption(exit, "Exit")
        tick(10)

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertNull(player.get<String>("random_event"))
        assertNull(player.instance())
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }
}
