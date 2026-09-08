package content.minigame.mage_training_arena

import WorldTest
import content.quest.instance
import interfaceOnFloorItem
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.StepValidator
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import skipDialogues
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

internal class TelekineticTheatreTest : WorldTest() {

    @Test
    fun `Entering the portal instances the first unsolved maze`() {
        val player = enter("mta-maze-enter")

        assertNotNull(player.instance())
        assertEquals("telekinetic", player["mage_training_arena_room", ""])
        assertEquals(1, player["mage_training_arena_telekinetic_maze", 0])
        assertEquals(TelekineticTheatre.local(player, Tile(8, 54)), player.tile)
        assertEquals(TelekineticTheatre.local(player, Tile(15, 41)), TelekineticTheatre.statue(player)?.tile)
        assertTrue(player.hasOpen("mage_training_arena_telekinetic"))
        assertTrue(NPCs.any { it.id == "telekinetic_guardian" && it.tile == TelekineticTheatre.local(player, Tile(8, 39)) })
    }

    @Test
    fun `Telegrab slides the statue towards the side the player stands on`() {
        val player = enter("mta-maze-slide")
        player.inventory.add("law_rune", 5)
        player.inventory.add("air_rune", 5)
        player.tele(TelekineticTheatre.local(player, Tile(15, 51)))
        tick(2)
        val statue = TelekineticTheatre.statue(player)!!
        val before = statue.tile

        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", statue)
        tick(15)

        val after = TelekineticTheatre.statue(player)!!.tile
        assertTrue(after.y > before.y) { "Statue didn't move north: $before -> $after" }
        assertEquals(before.x, after.x)
        assertEquals(4, player.inventory.count("law_rune"))
        assertEquals(43.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Reaching the exit square awards points and frees the guardian`() {
        val player = enter("mta-maze-win")
        player.inventory.add("law_rune", 5)
        player.inventory.add("air_rune", 5)
        val end = solve(player)
        tick(15)
        player.skipDialogues()
        tick(1)

        assertEquals(2, PizazzPoints.get(player, "telekinetic"))
        assertEquals(1, player["mage_training_arena_maze_streak", 0])
        assertEquals(1023, player["mage_training_arena_mazes_solved", 0])
        assertNull(TelekineticTheatre.statue(player))
        assertTrue(NPCs.any { it.id == "maze_guardian" && it.tile == end })
    }

    @Test
    fun `Fifth maze in a row gives bonus points, law runes and experience`() {
        val player = enter("mta-maze-bonus")
        player.inventory.add("law_rune", 5)
        player.inventory.add("air_rune", 5)
        player["mage_training_arena_maze_streak"] = 4
        solve(player)
        tick(15)
        player.skipDialogues()
        tick(1)

        assertEquals(10, PizazzPoints.get(player, "telekinetic"))
        assertEquals(0, player["mage_training_arena_maze_streak", 0])
        assertEquals(14, player.inventory.count("law_rune"))
        assertEquals(1043.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Exit portal frees the instance and returns to the lobby`() {
        val player = enter("mta-maze-exit")
        val base = TelekineticTheatre.local(player, Tile(0, 0))
        val exit = MageTrainingArenaTest.findObject("exit_portal_mage_training_arena", base, base.add(63, 63))
        player.tele(exit.tile.addY(-1))
        tick(2)

        player.objectOption(exit, "Enter")
        tick(10)

        assertNull(player.instance())
        assertEquals(Tile(3363, 3316), player.tile)
    }

    /**
     * Places the statue one step from the exit and casts from the matching side of the maze.
     */
    private fun solve(player: Player): Tile {
        val end = TelekineticTheatre.local(player, Tile(19, 50))
        val validator = get<StepValidator>()
        val direction = listOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).first { direction ->
            val from = end.minus(direction)
            validator.canTravel(level = from.level, x = from.x, z = from.y, offsetX = direction.delta.x, offsetZ = direction.delta.y, size = 1, extraFlag = 0, collision = CollisionStrategies.Normal)
        }
        val statueTile = end.minus(direction)
        TelekineticTheatre.placeStatue(player, statueTile)
        val origin = TelekineticTheatre.local(player, Tile(0, 0))
        val side = when (direction) {
            Direction.NORTH -> Tile(statueTile.x, origin.y + 51 + 1, statueTile.level)
            Direction.SOUTH -> Tile(statueTile.x, origin.y + 40 - 1, statueTile.level)
            Direction.EAST -> Tile(origin.x + 20 + 1, statueTile.y, statueTile.level)
            else -> Tile(origin.x + 9 - 1, statueTile.y, statueTile.level)
        }
        player.tele(side)
        tick(2)
        player.interfaceOnFloorItem("modern_spellbook", "telekinetic_grab", TelekineticTheatre.statue(player)!!)
        return end
    }

    private fun enter(name: String): Player {
        val player = createPlayer(Tile(3361, 3316), name)
        player.levels.set(Skill.Magic, 50)
        player["mage_training_arena_started"] = true
        player.inventory.add("progress_hat")
        player["mage_training_arena_mazes_solved"] = 1022
        val portal = MageTrainingArenaTest.findObject("telekinetic_portal", Tile(3355, 3310), Tile(3372, 3325))
        player.objectOption(portal, "Enter")
        tick(10)
        assertEquals("telekinetic", player["mage_training_arena_room", ""])
        return player
    }
}
