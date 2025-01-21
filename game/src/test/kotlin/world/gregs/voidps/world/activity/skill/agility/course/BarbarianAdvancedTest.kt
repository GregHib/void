package world.gregs.voidps.world.activity.skill.agility.course

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.objectOption

internal class BarbarianAdvancedTest : WorldTest() {

    @Test
    fun `Run up barbarian outpost run wall`() {
        val player = createPlayer("barbarian", Tile(2538, 3544))
        player.levels.set(Skill.Agility, 90)
        val pipe = objects[Tile(2538, 3541), "barbarian_outpost_run_wall"]!!

        player.objectOption(pipe, "Run-up")
        tick(13)

        assertEquals(Tile(2538, 3545, 2), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't do advanced barbarian outpost course`() {
        val player = createPlayer("barbarian", Tile(2538, 3542))
        val pipe = objects[Tile(2538, 3541), "barbarian_outpost_run_wall"]!!

        player.objectOption(pipe, "Run-up")
        tick(13)

        assertEquals(Tile(2538, 3542), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb up wall`() {
        val player = createPlayer("barbarian", Tile(2537, 3545, 2))
        val pipe = objects[Tile(2537, 3546, 2), "barbarian_outpost_climb_wall"]!!

        player.objectOption(pipe, "Climb-up")
        tick(6)

        assertEquals(Tile(2536, 3546, 3), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fire spring device`() {
        val player = createPlayer("barbarian", Tile(2536, 3546, 3))
        val pipe = objects[Tile(2532, 3544, 3), "barbarian_outpost_spring"]!!

        player.objectOption(pipe, "Fire")
        tick(10)

        assertEquals(Tile(2532, 3553, 3), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Cross balance beam`() {
        val player = createPlayer("barbarian", Tile(2532, 3553, 3))
        val pipe = objects[Tile(2534, 3553, 3), "barbarian_outpost_balance_beam"]!!

        player.objectOption(pipe, "Cross")
        tick(6)

        assertEquals(Tile(2536, 3553, 3), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Jump over gap`() {
        val player = createPlayer("barbarian", Tile(2536, 3553, 3))
        val pipe = objects[Tile(2537, 3553, 3), "barbarian_outpost_gap"]!!

        player.objectOption(pipe, "Jump-over")
        tick(3)

        assertEquals(Tile(2539, 3553, 2), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Slide down roof`() {
        val player = createPlayer("barbarian", Tile(2539, 3553, 2))
        val pipe = objects[Tile(2540, 3553, 2), "barbarian_outpost_roof"]!!

        player.objectOption(pipe, "Slide-down")
        tick(6)

        assertEquals(Tile(2544, 3553), player.tile)
        assertEquals(15.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Finish advanced course lap`() {
        val player = createPlayer("barbarian", Tile(2538, 3552, 2))
        val pipe = objects[Tile(2540, 3552, 2), "barbarian_outpost_roof"]!!

        player.agilityCourse("barbarian")
        player.agilityStage = 7
        player.objectOption(pipe, "Slide-down")
        tick(7)

        assertEquals(Tile(2544, 3552), player.tile)
        assertEquals(630.0, player.experience.get(Skill.Agility))
    }

}