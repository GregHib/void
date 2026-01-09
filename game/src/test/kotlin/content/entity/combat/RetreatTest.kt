package content.entity.combat

import WorldTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RetreatTest : WorldTest() {

    @TestFactory
    fun `Retreat directions`() = listOf(
        Direction.NORTH_WEST to Direction.SOUTH_EAST,
        Direction.NORTH to Direction.SOUTH_WEST,
        Direction.NORTH_EAST to Direction.SOUTH_WEST,
        Direction.EAST to Direction.SOUTH_WEST,
        Direction.SOUTH_EAST to Direction.NORTH_WEST,
        Direction.SOUTH to Direction.NORTH_WEST,
        Direction.SOUTH_WEST to Direction.NORTH_EAST,
        Direction.WEST to Direction.SOUTH_EAST,
    ).map { (from, to) ->
        dynamicTest("Retreat target $from moves $to") {
            npcs.clear()
            players.clear()
            val spawn = Tile(3035, 3355)
            val npc = createNPC("guard_falador", spawn)
            val player = createPlayer(tile = spawn.add(from))
            npc.mode = Retreat(npc, player, spawn, retreatRange = 4)
            tick()
            assertEquals(spawn.add(to), npc.tile)
        }
    }

    @Test
    fun `Retreat stops when target leaves 11 tiles outside of aggression range`() {
        val spawn = Tile(3036, 3355)
        val npc = createNPC("guard_falador", spawn)
        val player = createPlayer(tile = Tile(3021, 3358))
        npc.mode = Retreat(npc, player, spawn, retreatRange = 4)
        tick()
        player.walkTo(Tile(3020, 3358))
        tick(2)
        assertEquals(Tile(3020, 3358), player.tile)
        assertTrue(npc.mode is EmptyMode)
    }

    @TestFactory
    fun `Can't retreat out of bounds`() = listOf(
        Direction.NORTH_WEST,
        Direction.NORTH_EAST,
        Direction.SOUTH_EAST,
        Direction.SOUTH_WEST,
    ).map { direction ->
        dynamicTest("Can't retreat $direction out of bounds") {
            npcs.clear()
            players.clear()
            val spawn = Tile(3035, 3355)
            val npc = createNPC("guard_falador", spawn)
            val player = createPlayer(tile = spawn.add(direction.inverse()))
            npc.mode = Retreat(npc, player, spawn, retreatRange = 4)
            tick(6)
            assertEquals(spawn.add(direction.delta.x * 4, direction.delta.y * 4), npc.tile)
        }
    }
}