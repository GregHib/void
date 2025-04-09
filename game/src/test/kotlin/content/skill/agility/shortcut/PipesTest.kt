package content.skill.agility.shortcut

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

internal class PipesTest : WorldTest() {

    @Test
    fun `Squeeze north through brimhaven dungeon pipe`() {
        val player = createPlayer(tile = Tile(2655, 9566))
        player.levels.set(Skill.Agility, 22)
        val pipe = objects[Tile(2655, 9567), "pipe_brimhaven_dungeon"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        Assertions.assertEquals(Tile(2655, 9573), player.tile)
        Assertions.assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Squeeze south through brimhaven dungeon pipe`() {
        val player = createPlayer(tile = Tile(2655, 9573))
        player.levels.set(Skill.Agility, 22)
        val pipe = objects[Tile(2655, 9572), "pipe_brimhaven_dungeon"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        Assertions.assertEquals(Tile(2655, 9566), player.tile)
        Assertions.assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't squeeze through brimhaven pipe without level`() {
        val player = createPlayer(tile = Tile(2655, 9573))
        player.levels.set(Skill.Agility, 21)
        val pipe = objects[Tile(2655, 9572), "pipe_brimhaven_dungeon"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(2)

        Assertions.assertTrue(player.containsMessage("You need an Agility level of 22"))
    }
}