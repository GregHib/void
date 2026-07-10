package content.activity.event.random

import WorldTest
import interfaceOption
import npcOption
import org.junit.jupiter.api.Test
import skipDialogues
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SandwichLadyTest : WorldTest() {

    private val spot = Tile(3221, 3218)

    private fun setup(name: String, food: String): Pair<Player, NPC> {
        val player = createPlayer(spot, name)
        player["random_event"] = "sandwich_lady"
        player["random_event_origin"] = spot.id
        player["sandwich_lady_food"] = food
        val lady = createNPC("sandwich_lady", spot.addX(1))
        lady["owner"] = player.accountName
        player["sandwich_lady_npc"] = lady.index
        return player to lady
    }

    private fun openTray(player: Player, lady: NPC) {
        player.npcOption(lady, "Talk-to")
        tick()
        player.skipDialogues() // "You look hungry..." -> opens the tray
        tick()
    }

    @Test
    fun `Event spawns the sandwich lady beside the player with a chosen food`() {
        val player = createPlayer(spot, "sl_spawn")
        RandomEvents.start(player, "sandwich_lady")
        tick(2)

        assertTrue(player.get<String>("sandwich_lady_food") != null)
        val lady = (-2..2).flatMap { dx -> (-2..2).map { dy -> player.tile.add(dx, dy) } }
            .firstNotNullOfOrNull { t -> NPCs.firstOrNull(t) { it.id == "sandwich_lady" } }
        assertTrue(lady != null, "Expected a sandwich lady spawned near the player")
    }

    @Test
    fun `Choosing the offered food hands it over and ends the event in place`() {
        val (player, lady) = setup("sl_correct", "meat_pie")
        openTray(player, lady)

        assertTrue(player.interfaces.contains("sandwich_lady_select"))
        player.interfaceOption("sandwich_lady_select", "meat_pie", "Choose refreshment")
        tick()
        player.skipDialogues() // "Hope that fills you up!"
        tick()

        assertEquals(1, player.inventory.count("meat_pie"))
        assertEquals(spot, player.tile) // in-place: not teleported anywhere
        assertNull(player.get<String>("random_event"))
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `Choosing the wrong food knocks the player out and exiles them empty-handed`() {
        val (player, lady) = setup("sl_wrong", "meat_pie")
        player.inventory.add("logs", 4)
        openTray(player, lady)

        player.interfaceOption("sandwich_lady_select", "baguette", "Choose refreshment")
        tick(5) // baguette swing, knockout animation, screen fade, then teleport

        assertEquals(0, player.inventory.count("baguette"))
        assertEquals(4, player.inventory.count("logs")) // knockout doesn't note items
        assertNull(player.get<String>("random_event"))
        assertTrue(player.contains("random_event_cooldown"))
    }

    @Test
    fun `Another player can't take the sandwich`() {
        val (owner, lady) = setup("sl_owner", "roll")
        val intruder = createPlayer(spot.addY(1), "sl_intruder")

        intruder.npcOption(lady, "Talk-to")
        tick()

        assertFalse(intruder.interfaces.contains("sandwich_lady_select"))
    }
}
