package world.gregs.voidps.world.activity.achievement

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.objectOption
import kotlin.test.assertTrue

internal class LumbridgeBeginnerTasksTest : WorldTest() {

    @Test
    fun `On the Run`() = runTest {
        val player = createPlayer("adventurer", emptyTile)

        player.running = true
        player.instructions.send(Walk(emptyTile.x, emptyTile.y + 2))
        tick()

        assertTrue(player["on_the_run_task", false])
    }

    @Test
    fun `A World in Microcosm`() = runTest {
        val player = createPlayer("adventurer", emptyTile)

        player.instructions.send(Walk(emptyTile.x + 1, emptyTile.y + 1, minimap = true))
        tick()

        assertTrue(player["a_world_in_microcosm_task", false])
    }

    @Test
    fun `Master of All I survey`() = runTest {
        val player = createPlayer("adventurer", Tile(3207, 3224, 2))
        val ladder = objects[Tile(3207, 3223, 2), "lumbridge_castle_ladder"]!!

        player.objectOption(ladder, "Climb-up")
        tick(3)

        assertTrue(player["master_of_all_i_survey_task", false])
    }

    @Test
    fun `Raise the Roof`() = runTest {
        val player = createPlayer("adventurer", Tile(3209, 3217, 3))
        val ladder = objects[Tile(3210, 3218, 3), "lumbridge_flag"]!!

        player.objectOption(ladder, "Raise")
        tick(25)

        assertTrue(player["raise_the_roof_task", false])
    }

    @Test
    fun `Take Your Pick`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 256) until else 0
        })
        val player = createPlayer("adventurer", Tile(3229, 3147))
        player.levels.set(Skill.Mining, 100)
        val rocks = objects[Tile(3230, 3147), "copper_rocks_rock_1"]!!
        player.inventory.add("bronze_pickaxe")

        player.objectOption(rocks, "Mine")
        tick(9)

        assertTrue(player["take_your_pick_task", false])
    }

    @Test
    fun `Adventurer's Log`() {
        val player = createPlayer("adventurer", Tile(3233, 3215))
        player.levels.set(Skill.Woodcutting, 100)
        val tree = objects[Tile(3233, 3216), "tree_4"]!!
        player.inventory.add("bronze_hatchet")

        player.objectOption(tree, "Chop down")
        tick(4)

        assertTrue(player["adventurers_log_task", false])
    }
}