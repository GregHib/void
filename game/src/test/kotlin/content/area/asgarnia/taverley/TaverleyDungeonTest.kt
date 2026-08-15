package content.area.asgarnia.taverley

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class TaverleyDungeonTest : WorldTest() {

    @Test
    fun `Taverley dungeon pipe shortcut east`() {
        val player = createPlayer(Tile(2886, 9799))
        player.levels.set(Skill.Agility, 70)
        val pipe = GameObjects.find(Tile(2887, 9799), "taverly_dungeon_pipe_sc")

        player.objectOption(pipe, "Squeeze-through")
        tick(7)

        assertEquals(Tile(2892, 9799), player.tile)
        assertEquals(10.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Taverley dungeon pipe shortcut without level`() {
        val player = createPlayer(Tile(2886, 9799))
        val pipe = GameObjects.find(Tile(2887, 9799), "taverly_dungeon_pipe_sc")

        player.objectOption(pipe, "Squeeze-through")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 70"))
    }

    @Test
    fun `Taverley dungeon pipe shortcut west`() {
        val player = createPlayer(Tile(2892, 9799))
        player.levels.set(Skill.Agility, 70)
        val pipe = GameObjects.find(Tile(2891, 9799), "taverly_dungeon_pipe_sc")

        player.objectOption(pipe, "Squeeze-through")
        tick(7)

        assertEquals(Tile(2886, 9799), player.tile)
        assertEquals(10.0, player.experience.get(Skill.Agility))
    }
}
