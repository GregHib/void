package content.activity.event.random

import WorldTest
import interfaceOption
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PilloryTest : WorldTest() {

    private val origin = Tile(3221, 3218)
    private val cages = listOf(
        Tile(3228, 3408),
        Tile(3230, 3408),
        Tile(2681, 3488),
        Tile(2683, 3488),
        Tile(2685, 3488),
        Tile(2608, 3104),
    )

    private fun enter(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "pillory")
        tick(8)
        return player
    }

    private fun openPuzzle(player: Player) {
        val cage = GameObjects.add("pillory_cage", player.tile)
        player.objectOption(cage, "Unlock")
        tick()
    }

    private fun Player.pickAnswer() {
        interfaceOption("pillory_lock", "button_${get("pillory_answer", 0)}", "Select")
        tick()
    }

    @Test
    fun `Guard arrests the player into a pillory cage`() {
        val player = enter("pil_start")

        assertEquals("pillory", player.get<String>("random_event"))
        // The player lands on (or, if the stocks block the tile, right beside) a pillory cage.
        assertTrue(cages.any { player.tile.within(it, 1) }, "Expected by a cage, was ${player.tile}")
        assertEquals(3, player.get("pillory_target", 0))
    }

    @Test
    fun `A correct key increments the streak`() {
        val player = enter("pil_correct")
        openPuzzle(player)

        player.pickAnswer()

        assertEquals(1, player.get("pillory_correct", 0))
    }

    @Test
    fun `A wrong key resets the streak and raises the target`() {
        val player = enter("pil_wrong")
        openPuzzle(player)

        val answer = player.get("pillory_answer", 0)
        val wrong = (1..3).first { it != answer }
        player.interfaceOption("pillory_lock", "button_$wrong", "Select")
        tick()

        assertEquals(0, player.get("pillory_correct", 0))
        assertEquals(4, player.get("pillory_target", 0))
    }

    @Test
    fun `Three correct keys free the player and reward loot`() {
        val player = enter("pil_finish")
        openPuzzle(player)

        repeat(3) { player.pickAnswer() }
        tick(5) // wait out the modern teleport takeoff

        assertFalse(player.inventory.isEmpty())
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }
}
