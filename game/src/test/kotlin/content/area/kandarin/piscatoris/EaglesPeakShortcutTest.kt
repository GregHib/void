package content.area.kandarin.piscatoris

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class EaglesPeakShortcutTest : WorldTest() {

    @Test
    fun `Climb up eagles peak`() {
        val player = createPlayer(tile = Tile(2322, 3502))
        player.levels.set(Skill.Agility, 25)
        val rocks = objects[Tile(2322, 3501), "eagles_peak_rocks"]!!

        player.objectOption(rocks, "Climb")
        tick(9)

        assertEquals(Tile(2324, 3497), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Climb down eagles peak`() {
        val player = createPlayer(tile = Tile(2324, 3497))
        player.levels.set(Skill.Agility, 25)
        val rocks = objects[Tile(2324, 3498), "eagles_peak_rocks"]!!

        player.objectOption(rocks, "Climb")
        tick(9)

        assertEquals(Tile(2322, 3502), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't climb without level`() {
        val player = createPlayer(tile = Tile(2324, 3497))
        player.levels.set(Skill.Agility, 24)
        val rocks = objects[Tile(2323, 3497), "eagles_peak_rocks"]!!

        player.objectOption(rocks, "Climb")
        tick(2)

        assertTrue(player.containsMessage("You must have an Agility level of at least 25"))
    }
}