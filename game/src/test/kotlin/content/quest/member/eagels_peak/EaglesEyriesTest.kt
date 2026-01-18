package content.quest.member.eagels_peak

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class EaglesEyriesTest : WorldTest() {

    @Test
    fun `Climb up eagles peak`() {
        val player = createPlayer(Tile(2322, 3502))
        player.levels.set(Skill.Agility, 25)
        val rocks = objects.find(Tile(2322, 3501), "eagles_peak_rocks")

        player.objectOption(rocks, "Climb")
        tick(9)

        assertEquals(Tile(2324, 3497), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb down eagles peak`() {
        val player = createPlayer(Tile(2324, 3497))
        player.levels.set(Skill.Agility, 25)
        val rocks = objects.find(Tile(2324, 3498), "eagles_peak_rocks")

        player.objectOption(rocks, "Climb")
        tick(9)

        assertEquals(Tile(2322, 3502), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't climb eagels peak without level`() {
        val player = createPlayer(Tile(2324, 3497))
        player.levels.set(Skill.Agility, 24)
        val rocks = objects.find(Tile(2323, 3497), "eagles_peak_rocks")

        player.objectOption(rocks, "Climb")
        tick(2)

        assertTrue(player.containsMessage("You must have an Agility level of at least 25"))
    }

    @Test
    fun `Climb up rellekka rocky handholds`() {
        val player = createPlayer(Tile(2740, 3830, 1))
        player.levels.set(Skill.Agility, 35)
        val rocks = objects.find(Tile(2741, 3830, 1), "rocky_handholds_bottom")

        player.objectOption(rocks, "Climb")
        tick(9)

        assertEquals(Tile(2744, 3830, 1), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb down rellekka rocky handholds`() {
        val player = createPlayer(Tile(2744, 3830, 1))
        player.levels.set(Skill.Agility, 35)
        val rocks = objects.find(Tile(2743, 3830, 1), "rocky_handholds_top")

        player.objectOption(rocks, "Climb")
        tick(9)

        assertEquals(Tile(2740, 3830, 1), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't climb rellekkas rocky handholds without level`() {
        val player = createPlayer(Tile(2740, 3830, 1))
        player.levels.set(Skill.Agility, 34)
        val rocks = objects.find(Tile(2741, 3830, 1), "rocky_handholds_bottom")

        player.objectOption(rocks, "Climb")
        tick(2)

        assertTrue(player.containsMessage("You must have an Agility level of at least 35"))
    }
}
