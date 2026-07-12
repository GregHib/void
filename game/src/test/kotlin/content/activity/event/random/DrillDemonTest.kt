package content.activity.event.random

import WorldTest
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DrillDemonTest : WorldTest() {

    private val yard = Tile(3163, 4820)
    private val matTiles = listOf(Tile(3160, 4819), Tile(3162, 4819), Tile(3164, 4819), Tile(3166, 4819))

    private fun enter(name: String, origin: Tile = Tile(3221, 3218)): Player {
        val player = createPlayer(yard, name)
        player["random_event"] = "drill_demon"
        player["random_event_origin"] = origin.id
        player["drill_demon_correct"] = 0
        player["drill_demon_ready"] = true // the order has been given
        tick(2) // load the yard so the map mats resolve
        return player
    }

    private fun mats(): List<GameObject> = matTiles.mapIndexed { i, t -> GameObjects.find(t) { it.id == "drill_demon_mat_${i + 1}" } }

    // Deterministic signs: mat m shows exercise m (1-4); task points at one exercise.
    private fun Player.layout(task: Int) {
        for (m in 1..4) set("drill_demon_sign_$m", m)
        set("drill_demon_task", task)
    }

    /** Use whichever mat currently shows the assigned exercise, driving through the animation + order. */
    private fun useCorrectMat(player: Player, mats: List<GameObject>) {
        while (player.dialogue != null) player.skipDialogues() // close the previous round's order
        if (player.get<String>("random_event") != "drill_demon") return
        val task = player.get("drill_demon_task", 0)
        val index = (1..4).first { player.get("drill_demon_sign_$it", 0) == task }
        player.objectOption(mats[index - 1], "Use")
        // Wait for the exercise + sign sweep to finish and Damien to respond (or the event to complete).
        tickIf(60) { player.dialogue == null && player.get<String>("random_event") == "drill_demon" }
    }

    @Test
    fun `Event kidnaps the player to the yard and assigns exercises`() {
        val player = createPlayer(Tile(3221, 3218), "dd_start")
        RandomEvents.start(player, "drill_demon")
        tick(10)

        assertEquals("drill_demon", player.get<String>("random_event"))
        assertTrue(player.tile.within(yard, 4), "Expected the player in the yard, was ${player.tile}")
        assertTrue(player.get("drill_demon_task", 0) in 1..4)
        // All four exercises are shown across the four mats.
        assertEquals(setOf(1, 2, 3, 4), (1..4).map { player.get("drill_demon_sign_$it", 0) }.toSet())
    }

    @Test
    fun `Using the mat with the ordered exercise counts as correct`() {
        val player = enter("dd_correct")
        player.layout(task = 2) // exercise 2 is on mat 2
        player.objectOption(mats()[1], "Use")
        tickIf { player.dialogue == null }

        assertEquals(1, player.get("drill_demon_correct", 0))
    }

    @Test
    fun `Using a mat before the order is given gives no credit`() {
        val player = enter("dd_early")
        player.clear("drill_demon_ready") // order not given yet
        player.layout(task = 1)
        player.objectOption(mats()[0], "Use") // mat 1 would be correct once the order is given
        tickIf { player.dialogue == null } // Damien scolds instead

        assertEquals(0, player.get("drill_demon_correct", 0))
    }

    @Test
    fun `Using the wrong mat does not count`() {
        val player = enter("dd_wrong")
        player.layout(task = 2)
        player.objectOption(mats()[0], "Use") // mat 1 shows exercise 1, not the ordered 2
        tickIf { player.dialogue == null }

        assertEquals(0, player.get("drill_demon_correct", 0))
    }

    @Test
    fun `Four correct exercises reward a camo piece and return the player`() {
        val origin = Tile(3221, 3218)
        val player = enter("dd_finish", origin)
        player.layout(task = 1)
        val mats = mats()

        repeat(4) { useCorrectMat(player, mats) }
        while (player.dialogue != null) player.skipDialogues() // Damien's closing line, then reward
        tick(5) // wait out the modern teleport takeoff

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertEquals(1, player.get("camo_costume_points", 0))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
    }
}
