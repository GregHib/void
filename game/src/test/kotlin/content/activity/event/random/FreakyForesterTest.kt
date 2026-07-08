package content.activity.event.random

import WorldTest
import content.entity.combat.hit.damage
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreakyForesterTest : WorldTest() {

    private val clearing = Tile(2601, 4777)

    private fun startInClearing(name: String, task: Int, origin: Tile = Tile(3221, 3218)) = createPlayer(clearing, name).apply {
        this["random_event"] = "freaky_forester"
        this["random_event_origin"] = origin.id
        this["freaky_forester_task"] = task
    }

    @Test
    fun `Event teleports the player to the clearing and assigns a pheasant`() {
        val player = createPlayer(Tile(3221, 3218), "ff_start")
        RandomEvents.start(player, "freaky_forester")
        tick(10)

        assertEquals("freaky_forester", player.get<String>("random_event"))
        assertTrue(player.tile.within(clearing, 4), "Expected the player in the clearing, was ${player.tile}")
        assertTrue(player.get("freaky_forester_task", 0) in 1..4)
    }

    @Test
    fun `Killing the assigned pheasant yields the correct raw pheasant`() {
        val player = startInClearing("ff_correct_kill", task = 2)
        val pheasant = createNPC("pheasant_2_tails", Tile(2603, 4777))

        pheasant.damage(1000, source = player)
        tick(4)

        assertTrue(player.inventory.contains("raw_pheasant"))
        assertFalse(player.inventory.contains("raw_pheasant_incorrect"))
    }

    @Test
    fun `Killing the wrong pheasant yields an incorrect raw pheasant`() {
        val player = startInClearing("ff_wrong_kill", task = 2)
        val pheasant = createNPC("pheasant_1_tail", Tile(2603, 4777))

        pheasant.damage(1000, source = player)
        tick(4)

        assertTrue(player.inventory.contains("raw_pheasant_incorrect"))
        assertFalse(player.inventory.contains("raw_pheasant"))
    }

    @Test
    fun `Handing in the correct pheasant rewards a costume piece and returns the player`() {
        val origin = Tile(3221, 3218)
        val player = startInClearing("ff_handin", task = 3, origin = origin)
        player.inventory.add("raw_pheasant")
        val forester = createNPC("freaky_forester", Tile(2601, 4776))

        player.npcOption(forester, "Talk-to")
        tick()
        player.skipDialogues()
        tick(2)

        assertEquals(1, player.inventory.count("lederhosen_hat"))
        assertFalse(player.inventory.contains("raw_pheasant"))
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
    }

    @Test
    fun `Handing in the wrong pheasant is rejected and keeps the player in the event`() {
        val player = startInClearing("ff_reject", task = 3)
        player.inventory.add("raw_pheasant_incorrect")
        val forester = createNPC("freaky_forester", Tile(2601, 4776))

        player.npcOption(forester, "Talk-to")
        tick()
        player.skipDialogues()

        assertFalse(player.inventory.contains("raw_pheasant_incorrect"))
        assertEquals("freaky_forester", player.get<String>("random_event"))
        assertEquals(clearing, player.tile)
    }

    @Test
    fun `Owning the full lederhosen set rewards coins instead`() {
        val player = startInClearing("ff_coins", task = 1)
        player.inventory.add("lederhosen_hat")
        player.inventory.add("lederhosen_top")
        player.inventory.add("lederhosen_shorts")
        player.inventory.add("raw_pheasant")
        val forester = createNPC("freaky_forester", Tile(2601, 4776))

        player.npcOption(forester, "Talk-to")
        tick()
        player.skipDialogues()
        tick(2)

        assertEquals(500, player.inventory.count("coins"))
    }

    @Test
    fun `Cannot attack another pheasant while carrying a raw pheasant`() {
        val player = startInClearing("ff_block", task = 2)
        player.inventory.add("raw_pheasant")
        val pheasant = createNPC("pheasant_4_tails", Tile(2603, 4777))

        assertFalse(CombatApi.canAttack(player, pheasant))
    }
}
