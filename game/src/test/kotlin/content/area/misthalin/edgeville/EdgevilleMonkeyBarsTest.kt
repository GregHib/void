package content.area.misthalin.edgeville

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

class EdgevilleMonkeyBarsTest : WorldTest() {

    @Test
    fun `Swing south across monkey bars`() {
        val player = createPlayer(Tile(3120, 9970))
        player.levels.set(Skill.Agility, 15)
        val monkeyBars = objects[Tile(3119, 9969), "edgeville_monkey_bars"]!!

        player.objectOption(monkeyBars, "Swing across")
        tick(12)

        assertEquals(Tile(3120, 9964), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Swing north across monkey bars`() {
        val player = createPlayer(Tile(3121, 9964))
        player.levels.set(Skill.Agility, 15)
        val monkeyBars = objects[Tile(3120, 9964), "edgeville_monkey_bars"]!!

        player.objectOption(monkeyBars, "Swing across")
        tick(11)

        assertEquals(Tile(3121, 9969), player.tile)
        assertEquals(20.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't swing across monkey bars without level`() {
        val player = createPlayer(Tile(3121, 9964))
        val monkeyBars = objects[Tile(3120, 9964), "edgeville_monkey_bars"]!!

        player.objectOption(monkeyBars, "Swing across")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 15"))
    }
}
