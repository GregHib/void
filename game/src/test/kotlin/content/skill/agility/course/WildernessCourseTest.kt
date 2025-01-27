package content.skill.agility.course

import content.skill.agility.course.agilityCourse
import content.skill.agility.course.agilityStage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import FakeRandom
import WorldTest
import objectOption
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

internal class WildernessCourseTest : WorldTest() {

    @Test
    fun `Enter course`() {
        val player = createPlayer("agile", Tile(2998, 3916))
        player.levels.set(Skill.Agility, 52)
        val door = objects[Tile(2998, 3917), "wilderness_agility_door_closed"]!!

        player.objectOption(door, "Open")
        tick(17)

        assertEquals(Tile(2998, 3931), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Enter course without level`() {
        val player = createPlayer("agile", Tile(2998, 3916))
        val door = objects[Tile(2998, 3917), "wilderness_agility_door_closed"]!!

        player.objectOption(door, "Open")
        tick(3)

        assertEquals(Tile(2998, 3916), player.tile)
    }

    @Test
    fun `Exit course`() {
        val player = createPlayer("agile", Tile(2998, 3931))
        val door = objects[Tile(2998, 3931), "wilderness_agility_gate_east_closed"]!!

        player.objectOption(door, "Open")
        tick(17)

        assertEquals(Tile(2998, 3916), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Exit course failure`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("agile", Tile(2998, 3931))
        val door = objects[Tile(2998, 3931), "wilderness_agility_gate_east_closed"]!!

        player.objectOption(door, "Open")
        tick(12)

        assertEquals(Tile(3001, 3923), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb through pipe`() {
        val player = createPlayer("agile", Tile(3004, 3937))
        val pipe = objects[Tile(3004, 3938), "wilderness_obstacle_pipe"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(10)

        assertEquals(Tile(3004, 3950), player.tile)
        assertEquals(12.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb through pipe reversed`() {
        val player = createPlayer("agile", Tile(3004, 3950))
        val pipe = objects[Tile(3004, 3948), "wilderness_obstacle_pipe"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(2)

        assertEquals(Tile(3004, 3950), player.tile)
    }

    @Test
    fun `Swing on rope swing`() {
        val player = createPlayer("agile", Tile(3005, 3953))
        val rope = objects[Tile(3005, 3952), "wilderness_rope_swing"]!!

        player.objectOption(rope, "Swing-on")
        tick(6)

        assertEquals(Tile(3005, 3958), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fall off rope swing`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("agile", Tile(3005, 3953))
        val rope = objects[Tile(3005, 3952), "wilderness_rope_swing"]!!

        player.objectOption(rope, "Swing-on")
        tick(7)

        assertEquals(Tile(3004, 10357), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(75, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Cross stepping stones`() {
        val player = createPlayer("agile", Tile(3002, 3960))
        val stones = objects[Tile(3001, 3960), "wilderness_stepping_stone"]!!

        player.objectOption(stones, "Cross")
        tick(14)

        assertEquals(Tile(2996, 3960), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fall off stepping stones`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("agile", Tile(3002, 3960))
        val stones = objects[Tile(3001, 3960), "wilderness_stepping_stone"]!!

        player.objectOption(stones, "Cross")
        tick(14)

        assertEquals(Tile(3002, 3963), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(70, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Cross log balance`() {
        val player = createPlayer("agile", Tile(3001, 3946))
        val stones = objects[Tile(3001, 3945), "wilderness_log_balance"]!!

        player.objectOption(stones, "Walk-across")
        tick(11)

        assertEquals(Tile(2994, 3945), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fall off log balance`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until
        })
        val player = createPlayer("agile", Tile(3001, 3946))
        val stones = objects[Tile(3001, 3945), "wilderness_log_balance"]!!

        player.objectOption(stones, "Walk-across")
        tick(11)

        assertEquals(Tile(2998, 10345), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(75, player.levels.get(Skill.Constitution))
    }

    @Test
    fun `Climb rocks`() {
        val player = createPlayer("agile", Tile(2994, 3937))
        val rocks = objects[Tile(2994, 3936), "wilderness_agility_rocks"]!!

        player.objectOption(rocks, "Climb")
        tick(6)

        assertEquals(Tile(2994, 3933), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(0, player["wilderness_course_laps", 0])
    }

    @Test
    fun `Climb through pipe with bonus reward`() {
        val player = createPlayer("agile", Tile(2995, 3937))
        val rocks = objects[Tile(2995, 3936), "wilderness_agility_rocks"]!!
        player.agilityCourse("wilderness")
        player.agilityStage = 4

        player.objectOption(rocks, "Climb")
        tick(6)

        assertEquals(Tile(2995, 3933), player.tile)
        assertEquals(499.0, player.experience.get(Skill.Agility))
        assertEquals(0, player.agilityStage)
        assertEquals(1, player["wilderness_course_laps", 0])
    }

}