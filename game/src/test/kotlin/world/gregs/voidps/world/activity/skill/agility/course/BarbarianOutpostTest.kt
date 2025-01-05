package world.gregs.voidps.world.activity.skill.agility.course

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.script.*

internal class BarbarianOutpostTest : WorldTest() {

    @Test
    fun `Enter entrance through pipe`() {
        val player = createPlayer("barbarian", Tile(2551, 3560))
        player.levels.set(Skill.Agility, 35)
        val pipe = objects[Tile(2552, 3559), "barbarian_outpost_entrance"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(7)

        assertEquals(Tile(2552, 3558), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail entrance through pipe`() {
        val player = createPlayer("barbarian", Tile(2551, 3560))
        val pipe = objects[Tile(2552, 3559), "barbarian_outpost_entrance"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(7)

        assertEquals(Tile(2551, 3560), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Swing across rope`() {
        val player = createPlayer("barbarian", Tile(2552, 3554))
        val rope = objects[Tile(2552, 3550), "barbarian_outpost_rope_swing"]!!

        player.objectOption(rope, "Swing-on")
        tick(7)

        assertEquals(Tile(2552, 3549), player.tile)
        assertEquals(22.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail swing across rope`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("barbarian", Tile(2552, 3554))
        val rope = objects[Tile(2552, 3550), "barbarian_outpost_rope_swing"]!!

        player.objectOption(rope, "Swing-on")
        tick(9)

        assertEquals(Tile(2552, 9949), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Walk across log`() {
        val player = createPlayer("barbarian", Tile(2551, 3546))
        val log = objects[Tile(2550, 3546), "barbarian_outpost_log_balance"]!!

        player.objectOption(log, "Walk-across")
        tick(12)

        assertEquals(Tile(2541, 3546), player.tile)
        assertEquals(13.7, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail to walk across log`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("barbarian", Tile(2551, 3546))
        val log = objects[Tile(2550, 3546), "barbarian_outpost_log_balance"]!!

        player.objectOption(log, "Walk-across")
        tick(15)

        assertEquals(Tile(2545, 3543), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertTrue(player.levels.get(Skill.Constitution) < 100)
    }

    @Test
    fun `Climb net`() {
        val player = createPlayer("barbarian", Tile(2539, 3546))
        val log = objects[Tile(2538, 3545), "barbarian_outpost_obstacle_net"]!!

        player.objectOption(log, "Climb-over")
        tick(3)

        assertEquals(Tile(2537, 3546, 1), player.tile)
        assertEquals(8.2, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Walk across ledge`() {
        val player = createPlayer("barbarian", Tile(2536, 3547, 1))
        val ledge = objects[Tile(2535, 3547, 1), "barbarian_outpost_balancing_ledge"]!!

        player.objectOption(ledge, "Walk-across")
        tick(7)

        assertEquals(Tile(2532, 3547, 1), player.tile)
        assertEquals(22.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail to walk across ledge`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("barbarian", Tile(2536, 3547, 1))
        val ledge = objects[Tile(2535, 3547, 1), "barbarian_outpost_balancing_ledge"]!!

        player.objectOption(ledge, "Walk-across")
        tick(9)

        assertEquals(Tile(2534, 3545), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertTrue(player.levels.get(Skill.Constitution) < 100)
    }

    @Test
    fun `Climb wall`() {
        val player = createPlayer("barbarian", Tile(2537, 3554))
        val wall = objects[Tile(2537, 3553), "barbarian_outpost_crumbling_wall"]!!

        player.objectOption(wall, "Climb-over")
        tick(5)

        assertEquals(Tile(2538, 3553), player.tile)
        assertEquals(13.7, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Finish course lap`() {
        val player = createPlayer("barbarian", Tile(2541, 3553))
        val wall = objects[Tile(2542, 3553), "barbarian_outpost_crumbling_wall"]!!

        player.agilityCourse("barbarian")
        player.agilityStage = 5
        player.objectOption(wall, "Climb-over")
        tick(3)

        assertEquals(Tile(2543, 3553), player.tile)
        assertEquals(60.0, player.experience.get(Skill.Agility))
        assertEquals(0, player.agilityStage)
    }

}