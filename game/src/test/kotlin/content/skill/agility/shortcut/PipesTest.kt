package content.skill.agility.shortcut

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile

internal class PipesTest : WorldTest() {

    @Test
    fun `Squeeze north through brimhaven moss dungeon pipe`() {
        val player = createPlayer(tile = Tile(2655, 9566))
        player.levels.set(Skill.Agility, 22)
        val pipe = objects[Tile(2655, 9567), "brimhaven_pipe_moss"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        assertEquals(Tile(2655, 9573), player.tile)
        assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Squeeze south through brimhaven moss dungeon pipe`() {
        val player = createPlayer(tile = Tile(2655, 9573))
        player.levels.set(Skill.Agility, 22)
        val pipe = objects[Tile(2655, 9572), "brimhaven_pipe_moss"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        assertEquals(Tile(2655, 9566), player.tile)
        assertEquals(8.5, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't squeeze through brimhaven moss pipe without level`() {
        val player = createPlayer(tile = Tile(2655, 9566))
        player.levels.set(Skill.Agility, 21)
        val pipe = objects[Tile(2655, 9567), "brimhaven_pipe_moss"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 22"))
    }

    @Test
    fun `Squeeze north through brimhaven dragon dungeon pipe`() {
        val player = createPlayer(tile = Tile(2698, 9492))
        player.levels.set(Skill.Agility, 34)
        val pipe = objects[Tile(2698, 9493), "brimhaven_pipe_dragon"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        assertEquals(Tile(2698, 9499), player.tile)
        assertEquals(10.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Squeeze south through brimhaven dragon dungeon pipe`() {
        val player = createPlayer(tile = Tile(2698, 9499))
        player.levels.set(Skill.Agility, 34)
        val pipe = objects[Tile(2698, 9498), "brimhaven_pipe_dragon"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        assertEquals(Tile(2698, 9492), player.tile)
        assertEquals(10.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't squeeze through brimhaven dragon pipe without level`() {
        val player = createPlayer(tile = Tile(2698, 9499))
        player.levels.set(Skill.Agility, 33)
        val pipe = objects[Tile(2698, 9498), "brimhaven_pipe_dragon"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 34"))
    }

    @Test
    fun `Squeeze east through varrock dungeon pipe`() {
        val player = createPlayer(tile = Tile(3149, 9906))
        player.levels.set(Skill.Agility, 51)
        val pipe = objects[Tile(3150, 9906), "varrock_dungeon_pipe"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        assertEquals(Tile(3155, 9906), player.tile)
        assertEquals(10.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Squeeze west through varrock dungeon pipe`() {
        val player = createPlayer(tile = Tile(3155, 9906))
        player.levels.set(Skill.Agility, 51)
        val pipe = objects[Tile(3153, 9906), "varrock_dungeon_pipe"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(8)

        assertEquals(Tile(3149, 9906), player.tile)
        assertEquals(10.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't squeeze through varrock dungeon pipe without level`() {
        val player = createPlayer(tile = Tile(3155, 9906))
        player.levels.set(Skill.Agility, 50)
        val pipe = objects[Tile(3153, 9906), "varrock_dungeon_pipe"]!!

        player.objectOption(pipe, "Squeeze-through")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 51"))
    }
}