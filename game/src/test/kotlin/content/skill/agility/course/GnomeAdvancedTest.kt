package content.skill.agility.course

import content.skill.agility.course.agilityCourse
import content.skill.agility.course.agilityStage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import FakeRandom
import WorldTest
import objectOption
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom

internal class GnomeAdvancedTest : WorldTest() {

    @Test
    fun `Climb up advanced branch`() {
        val player = createPlayer("agile", Tile(2472, 3420, 2))
        player.levels.set(Skill.Agility, 85)
        val branch = objects[Tile(2472, 3419, 2), "gnome_tree_branch_advanced"]!!

        player.objectOption(branch, "Climb-up")
        tick(3)

        assertEquals(Tile(2472, 3419, 3), player.tile)
        assertEquals(25.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't climb up advanced branch`() {
        val player = createPlayer("agile", Tile(2472, 3420, 2))
        val branch = objects[Tile(2472, 3419, 2), "gnome_tree_branch_advanced"]!!

        player.objectOption(branch, "Climb-up")
        tick(3)

        assertEquals(Tile(2472, 3420, 2), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Run across sign post`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 256) -10 else 0
        })
        val player = createPlayer("agile", Tile(2475, 3418, 3))
        val sign = objects[Tile(2478, 3417, 3), "gnome_sign_post_advanced"]!!

        player.objectOption(sign, "Run-across")
        tick(8)

        assertEquals(Tile(2484, 3418, 3), player.tile)
        assertEquals(25.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail to run across sign post`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = if (until == 1) 0 else until
        })
        val player = createPlayer("agile", Tile(2475, 3418, 3))
        val sign = objects[Tile(2478, 3417, 3), "gnome_sign_post_advanced"]!!

        player.objectOption(sign, "Run-across")
        tick(22)

        assertEquals(Tile(2484, 3418, 3), player.tile)
        assertTrue(player.levels.get(Skill.Constitution) < 100)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Swing across poles`() {
        val player = createPlayer("agile", Tile(2485, 3418, 3))
        val pole = objects[Tile(2486, 3425, 3), "gnome_pole_advanced"]!!

        player.objectOption(pole, "Swing-to")
        tick(16)

        assertEquals(Tile(2485, 3432, 3), player.tile)
        assertEquals(25.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Jump over barrier`() {
        val player = createPlayer("agile", Tile(2486, 3432, 3))
        val barrier = objects[Tile(2485, 3433, 3), "gnome_barrier_advanced"]!!

        player.objectOption(barrier, "Jump-over")
        tick(5)

        assertEquals(Tile(2485, 3436), player.tile)
        assertEquals(25.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Jump over barrier with bonus reward`() {
        val player = createPlayer("agile", Tile(2486, 3432, 3))
        val barrier = objects[Tile(2485, 3433, 3), "gnome_barrier_advanced"]!!
        player.agilityCourse("gnome")
        player.agilityStage = 6
        player.objectOption(barrier, "Jump-over")
        tick(5)

        assertEquals(Tile(2485, 3436), player.tile)
        assertEquals(630.0, player.experience.get(Skill.Agility))
        assertEquals(0, player.agilityStage)
    }

}