package content.activity.event.random

import WorldTest
import content.quest.instance
import content.quest.instanceOffset
import dialogueOption
import interfaceOption
import npcOption
import objectOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EvilTwinTest : WorldTest() {

    private val origin = Tile(3221, 3218)

    private fun start(name: String): Pair<Player, NPC> {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "evil_twin")
        tick(2)
        val molly = (-2..2).flatMap { dx -> (-2..2).map { dy -> player.tile.add(dx, dy) } }
            .firstNotNullOfOrNull { tile -> NPCs.firstOrNull(tile) { it.id.startsWith("molly_") } }
        assertNotNull(molly, "Expected Molly spawned near the player")
        return player to molly
    }

    private fun enterHouse(name: String): Player {
        val player = createPlayer(origin, name)
        RandomEvents.start(player, "evil_twin")
        tick(10) // Molly appears, pleads, and teleports the player away
        player.skipDialogues() // Her explanation in the house...
        player.dialogueOption(2) // ..."I'll get right to it."
        tick()
        return player
    }

    private fun Player.openCrane() {
        val panel = GameObjects.add("control_panel", tile)
        objectOption(panel, "Use")
        tick(2)
    }

    private fun Player.clawTile(): Tile = Tile(get("evil_twin_claw_x", 0), get("evil_twin_claw_y", 0)).add(instanceOffset())

    private fun Player.suspects(): List<NPC> = NPCs.at(tile.regionLevel).filter { it.id.startsWith("twin_suspect_") }

    private fun Player.grab(suspect: NPC?) {
        if (suspect != null) {
            suspect.mode = EmptyMode
            suspect.tele(clawTile())
            tick()
        }
        interfaceOption("evil_twin_crane", "grab", "Grab")
        tick(25) // The claw sequence walks the suspect over to the jail
    }

    @Test
    fun `Event spawns Molly beside the player`() {
        val (player, molly) = start("et_spawn")

        assertEquals("evil_twin", player.get<String>("random_event"))
        assertEquals(player.get("evil_twin_hash", 0), molly.id.removePrefix("molly_").toInt())
    }

    @Test
    fun `Molly takes the player to her house`() {
        val player = enterHouse("et_house")

        assertNotNull(player.instance())
        val suspects = player.suspects()
        assertEquals(5, suspects.size)
        assertTrue(
            suspects.any { it.id == "twin_suspect_${player.get("evil_twin_hash", 0)}" },
            "Expected one suspect matching Molly's look",
        )
    }

    @Test
    fun `The claw moves around the pen but not beyond it`() {
        val player = enterHouse("et_claw")
        player.openCrane()
        assertTrue(player.interfaces.contains("evil_twin_crane"))
        val home = player.clawTile()

        player.interfaceOption("evil_twin_crane", "up", "Up") // South, over the pen
        tick()
        assertEquals(home.addY(-1), player.clawTile())
        assertTrue(GameObjects.at(player.clawTile()).any { it.id == "evil_twin_claw" })
        assertTrue(GameObjects.at(player.clawTile()).any { it.id == "evil_twin_claw_marker" })
        assertTrue(GameObjects.at(home).none { it.id == "evil_twin_claw" }, "claw left behind at home")
        assertTrue(GameObjects.at(home).none { it.id == "evil_twin_claw_marker" }, "marker left behind at home")

        player.interfaceOption("evil_twin_crane", "down", "Down") // Back north to home
        tick()
        player.interfaceOption("evil_twin_crane", "down", "Down") // The north edge of its reach
        tick()
        assertEquals(home.addY(1), player.clawTile())

        player.interfaceOption("evil_twin_crane", "down", "Down") // Past the edge - blocked
        tick()
        assertEquals(home.addY(1), player.clawTile())
    }

    @Test
    fun `Grabbing the twin cages her and Molly pays out`() {
        val player = enterHouse("et_catch")
        player.openCrane()

        val twin = player.suspects().first { it.id == "twin_suspect_${player.get("evil_twin_hash", 0)}" }
        player.grab(twin)
        player.skipDialogues() // "You've caught Molly's evil twin!..."
        tick()

        assertTrue(player.get("evil_twin_caught", false))
        assertEquals(1, player.suspects().size)
        // The claw only swings back home once she's been dropped at the cage
        val home = Tile(1870, 5132).add(player.instanceOffset())
        assertTrue(GameObjects.at(home).any { it.id == "evil_twin_claw" })
        assertTrue(GameObjects.at(home).any { it.id == "evil_twin_claw_marker" })

        val molly = NPCs.at(player.tile.regionLevel).first { it.id.startsWith("molly_") }
        player.npcOption(molly, "Talk-to")
        tick(2)
        player.skipDialogues()
        tick(2)

        assertFalse(player.inventory.isEmpty(), "Expected a gem reward from Molly")
        assertNull(player.get<String>("random_event"))
        assertEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `Catching the twin after a failed grab still succeeds`() {
        val player = enterHouse("et_retry")
        player.openCrane()

        val innocent = player.suspects().first { it.id != "twin_suspect_${player.get("evil_twin_hash", 0)}" }
        player.grab(innocent)
        assertEquals(1, player.get("evil_twin_tries", 0))

        // Second grab with the twin still wandering: the tile check happens on the click itself,
        // so she can't step off the mark before the claw drops.
        val twin = player.suspects().first { it.id == "twin_suspect_${player.get("evil_twin_hash", 0)}" }
        twin.tele(player.clawTile())
        player.interfaceOption("evil_twin_crane", "grab", "Grab")
        tick(25)

        assertTrue(player.get("evil_twin_caught", false), "Expected the twin to be caught on the second try")
    }

    @Test
    fun `Logging out mid-event puts the player back in the house`() {
        val player = enterHouse("et_relog")
        player.openCrane()
        player.grab(null) // One try used before "logging out"
        val hash = player.get("evil_twin_hash", 0)

        // A login restarts the active event, exactly like RandomEventKidnap.playerSpawn
        RandomEvents.start(player, "evil_twin")
        tick(8)

        assertNotNull(player.instance())
        assertEquals(hash, player.get("evil_twin_hash", 0))
        assertEquals(1, player.get("evil_twin_tries", 0))
        assertEquals(5, player.suspects().size)
    }

    @Test
    fun `Two failed grabs get the player thrown out with nothing`() {
        val player = enterHouse("et_fail")
        player.openCrane()

        player.grab(null) // Nobody on the mark
        assertEquals(1, player.get("evil_twin_tries", 0))

        val innocent = player.suspects().first { it.id != "twin_suspect_${player.get("evil_twin_hash", 0)}" }
        player.grab(innocent)
        player.skipDialogues() // "Such incompetence!..."
        tick(2)

        assertTrue(player.inventory.isEmpty(), "Expected no reward after failing")
        assertNull(player.get<String>("random_event"))
        assertNotEquals(origin, player.tile)
        assertTrue(player.contains("random_event_cooldown"))
    }
}
