package content.activity.event.random

import WorldTest
import containsMessage
import content.quest.instance
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.Spawn
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MazeTest : WorldTest() {

    @Test
    fun `Touching the shrine rewards and returns the player`() {
        val player = createPlayer(Tile(3221, 3218), "maze_complete")
        RandomEvents.start(player, "maze")
        tick(10)

        assertNotNull(player.instance())
        assertTrue(player.interfaces.contains("maze_timer"))

        val shrine = createObject("strange_shrine", player.tile.addY(1))
        player.objectOption(shrine, "Touch")
        tick(10)

        assertNull(player.instance())
        assertEquals(Tile(3221, 3218), player.tile)
        assertNull(player.get<String>("random_event"))
        assertTrue(player.contains("random_event_cooldown"))
        assertEquals(1, player.inventory.count("random_event_gift"))
    }

    @Test
    fun `Touching the shrine again mid-teleport can't dupe the gift`() {
        val player = createPlayer(Tile(3221, 3218), "maze_dupe")
        RandomEvents.start(player, "maze")
        tick(10)

        val shrine = createObject("strange_shrine", player.tile.addY(1))
        player.objectOption(shrine, "Touch")
        tick(5) // cheer done, mid teleport-out delay
        player.objectOption(shrine, "Touch")
        tick(15)

        assertNull(player.get<String>("random_event"))
        assertEquals(1, player.inventory.count("random_event_gift"))
    }

    @Test
    fun `Running out of time exiles the player without reward`() {
        val player = createPlayer(Tile(3221, 3218), "maze_timeout")
        RandomEvents.start(player, "maze")
        tick(10)
        assertNotNull(player.instance())

        tick(305)

        assertNull(player.instance())
        assertNull(player.get<String>("random_event"))
        assertEquals(0, player.inventory.count("coins"))
        assertTrue(player.tile.x in 3221..3223 && player.tile.y in 3217..3219, "Expected exile to Lumbridge, was at ${player.tile}")
    }

    @Test
    fun `Logging out mid-maze restarts the event on login`() {
        val player = createPlayer(Tile(3221, 3218), "maze_relog")
        RandomEvents.start(player, "maze")
        tick(10)
        assertNotNull(player.instance())
        val origin = Tile(player["random_event_origin", 0])

        logout(player)
        assertNull(player.instance())
        login(player)
        tick(10)

        assertEquals("maze", player.get<String>("random_event"))
        assertNotNull(player.instance())
        assertEquals(origin, Tile(player["random_event_origin", 0]))

        val shrine = createObject("strange_shrine", player.tile.addY(1))
        player.objectOption(shrine, "Touch")
        tick(10)

        assertEquals(Tile(3221, 3218), player.tile)
    }

    @Test
    fun `Maze walls swing open and close behind the player`() {
        val player = createPlayer(Tile(3221, 3218), "maze_door")
        val door = createObject("maze_door_1_closed", Tile(3221, 3219), shape = ObjectShape.WALL_STRAIGHT)

        player.objectOption(door, "Open")
        tickIf { GameObjects.findOrNull(Tile(3221, 3219), "maze_door_1_closed") != null }

        assertNull(GameObjects.findOrNull(Tile(3221, 3219), "maze_door_1_closed"))
        tickIf { GameObjects.findOrNull(Tile(3221, 3219), "maze_door_1_closed") == null }
        assertNotNull(GameObjects.findOrNull(Tile(3221, 3219), "maze_door_1_closed"))
    }

    @Test
    fun `Chests give a small reward once opened`() {
        val player = createPlayer(Tile(3221, 3218), "maze_chest")
        player["maze_ticks"] = 100
        val chest = createObject("maze_chest", Tile(3221, 3219))

        player.objectOption(chest, "Open")
        tick(3)

        assertEquals(15, player.inventory.count("air_rune"))
        assertEquals(1, player["maze_chests_opened", 0])
        assertNull(GameObjects.findOrNull(Tile(3221, 3219), "maze_chest"))
        assertNotNull(GameObjects.findOrNull(Tile(3221, 3219), "maze_chest_opened"))
    }

    @Test
    fun `Chests give nothing once the cap is reached`() {
        val player = createPlayer(Tile(3221, 3218), "maze_chest_cap")
        player["maze_ticks"] = 100
        player["maze_chests_opened"] = 10
        val chest = createObject("maze_chest", Tile(3221, 3219))

        player.objectOption(chest, "Open")
        tick(3)

        assertEquals(0, player.inventory.count("air_rune"))
        assertTrue(player.containsMessage("You find nothing of interest."))
        assertNotNull(GameObjects.findOrNull(Tile(3221, 3219), "maze_chest"))
    }

    private fun login(player: Player) {
        Players.add(player)
        Spawn.player(player)
    }

    private fun logout(player: Player) {
        Despawn.player(player)
        Players.remove(player)
    }
}
