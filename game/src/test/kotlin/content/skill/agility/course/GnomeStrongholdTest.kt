package content.skill.agility.course

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

internal class GnomeStrongholdTest : WorldTest() {

    @Test
    fun `Walk across balance log`() {
        val player = createPlayer(Tile(2474, 3437))
        val log = GameObjects.find(Tile(2474, 3435), "gnome_log_balance")

        player.objectOption(log, "Walk-across")
        tick(10)

        assertEquals(Tile(2474, 3429), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb first obstacle net`() {
        val player = createPlayer(Tile(2475, 3426))
        val net = GameObjects.find(Tile(2475, 3425), "gnome_obstacle_net")

        player.objectOption(net, "Climb-over")
        tick(3)

        assertEquals(Tile(2475, 3424, 1), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb first tree branch`() {
        val player = createPlayer(Tile(2474, 3422, 1))
        val branch = GameObjects.find(Tile(2473, 3422, 1), "gnome_tree_branch_up")

        player.objectOption(branch, "Climb")
        tick(3)

        assertEquals(Tile(2473, 3420, 2), player.tile)
        assertEquals(5.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Walk across tight rope`() {
        val player = createPlayer(Tile(2477, 3419, 2))
        val rope = GameObjects.find(Tile(2478, 3420, 2), "gnome_balancing_rope")

        player.objectOption(rope, "Walk-on")
        tick(9)

        assertEquals(Tile(2483, 3420, 2), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb down tree branch`() {
        val player = createPlayer(Tile(2486, 3418, 2))
        val branch = GameObjects.find(Tile(2486, 3419, 2), "gnome_tree_branch_down")

        player.objectOption(branch, "Climb-down")
        tick(3)

        assertEquals(Tile(2486, 3420), player.tile)
        assertEquals(5.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb over netting`() {
        val player = createPlayer(Tile(2487, 3424))
        val net = GameObjects.find(Tile(2487, 3426), "gnome_obstacle_net_free_standing")

        player.objectOption(net, "Climb-over")
        tick(4)

        assertEquals(Tile(2487, 3427), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb over netting backwards`() {
        val player = createPlayer(Tile(2484, 3427))
        val net = GameObjects.find(Tile(2483, 3426), "gnome_obstacle_net_free_standing")

        player.objectOption(net, "Climb-over")
        tick(3)

        assertEquals(Tile(2484, 3425), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb through pipe`() {
        val player = createPlayer(Tile(2488, 3431))
        val pipe = GameObjects.find(Tile(2487, 3431), "gnome_obstacle_pipe_east")

        player.objectOption(pipe, "Squeeze-through")
        tick(11)

        assertEquals(Tile(2487, 3437), player.tile)
        assertEquals(7.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb through pipe with bonus reward`() {
        val player = createPlayer(Tile(2483, 3430))
        val pipe = GameObjects.find(Tile(2483, 3431), "gnome_obstacle_pipe_west")
        player.agilityCourse("gnome")
        player.agilityStage = 6

        player.objectOption(pipe, "Squeeze-through")
        tick(10)

        assertEquals(Tile(2483, 3437), player.tile)
        assertEquals(46.5, player.experience.get(Skill.Agility))
    }
}
