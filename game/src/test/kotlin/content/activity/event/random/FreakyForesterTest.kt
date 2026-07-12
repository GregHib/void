package content.activity.event.random

import WorldTest
import content.entity.combat.hit.damage
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
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
    fun `Killing the assigned pheasant drops the correct raw pheasant for the killer`() {
        val player = startInClearing("ff_correct_kill", task = 2)
        val pheasant = createNPC("pheasant_2_tails", Tile(2603, 4777))
        val dropTile = pheasant.tile

        pheasant.damage(1000, source = player)
        tick(4)

        val drop = FloorItems.firstOrNull(dropTile, "raw_pheasant")
        assertNotNull(drop)
        assertEquals(player.name, drop.owner)
        assertNull(FloorItems.firstOrNull(dropTile, "raw_pheasant_incorrect"))
    }

    @Test
    fun `Killing the wrong pheasant drops an incorrect raw pheasant`() {
        val player = startInClearing("ff_wrong_kill", task = 2)
        val pheasant = createNPC("pheasant_1_tail", Tile(2603, 4777))
        val dropTile = pheasant.tile

        pheasant.damage(1000, source = player)
        tick(4)

        assertNotNull(FloorItems.firstOrNull(dropTile, "raw_pheasant_incorrect"))
        assertNull(FloorItems.firstOrNull(dropTile, "raw_pheasant"))
    }

    @Test
    fun `Handing in the correct pheasant rewards a gift and returns the player`() {
        val origin = Tile(3221, 3218)
        val player = startInClearing("ff_handin", task = 3, origin = origin)
        player.inventory.add("raw_pheasant")
        val forester = createNPC("freaky_forester", Tile(2601, 4776))

        player.npcOption(forester, "Talk-to")
        tick()
        player.skipDialogues()
        tick(5) // wait out the modern teleport takeoff

        assertEquals(1, player.inventory.count("random_event_gift"))
        assertEquals(1, player.get("lederhosen_costume_points", 0))
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
    fun `Cannot attack another pheasant while carrying a raw pheasant`() {
        val player = startInClearing("ff_block", task = 2)
        player.inventory.add("raw_pheasant")
        val pheasant = createNPC("pheasant_4_tails", Tile(2603, 4777))

        assertFalse(CombatApi.canAttack(player, pheasant))
    }
}
