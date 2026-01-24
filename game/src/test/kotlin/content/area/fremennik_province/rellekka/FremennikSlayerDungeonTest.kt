package content.area.fremennik_province.rellekka

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class FremennikSlayerDungeonTest : WorldTest() {

    @Test
    fun `Jump across chasm`() {
        val player = createPlayer(Tile(2777, 10002))
        player.levels.set(Skill.Agility, 81)

        val chasm = GameObjects.find(Tile(2769, 10002), "slayer_dungeon_chasm")
        player.objectOption(chasm, "Jump-across")
        tick(7)

        assertEquals(10.0, player.experience.get(Skill.Agility))
        assertTrue(player.containsMessage("Your feet skid as you land"))
        assertEquals(Tile(2768, 10002), player.tile)
    }

    @Test
    fun `Jump back across chasm`() {
        val player = createPlayer(Tile(2768, 10002))
        player.levels.set(Skill.Agility, 81)

        val chasm = GameObjects.find(Tile(2769, 10002), "slayer_dungeon_chasm")
        player.objectOption(chasm, "Jump-across")
        tick(7)

        assertEquals(10.0, player.experience.get(Skill.Agility))
        assertTrue(player.containsMessage("Your feet skid as you land"))
        assertEquals(Tile(2775, 10002), player.tile)
    }

    @Test
    fun `Can't jump across chasm without 81 agility`() {
        val player = createPlayer(Tile(2775, 10002))

        val chasm = GameObjects.find(Tile(2769, 10002), "slayer_dungeon_chasm")
        player.objectOption(chasm, "Jump-across")
        tick(2)

        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertTrue(player.containsMessage("You need an agility level of 81"))
    }

    @Test
    fun `Squeeze through crevice`() {
        val player = createPlayer(Tile(2735, 10008))
        player.levels.set(Skill.Agility, 62)

        val chasm = GameObjects.find(Tile(2734, 10008), "slayer_dungeon_crevice")
        player.objectOption(chasm, "Squeeze-through")
        tick(6)

        assertEquals(7.5, player.experience.get(Skill.Agility))
        assertEquals(Tile(2730, 10008), player.tile)
    }

    @Test
    fun `Squeeze back through crevice`() {
        val player = createPlayer(Tile(2730, 10008))
        player.levels.set(Skill.Agility, 62)

        val chasm = GameObjects.find(Tile(2731, 10008), "slayer_dungeon_crevice")
        player.objectOption(chasm, "Squeeze-through")
        tick(6)

        assertEquals(7.5, player.experience.get(Skill.Agility))
        assertEquals(Tile(2735, 10008), player.tile)
    }

    @Test
    fun `Can't use crevice without 62 agility`() {
        val player = createPlayer(Tile(2735, 10008))

        val chasm = GameObjects.find(Tile(2734, 10008), "slayer_dungeon_crevice")
        player.objectOption(chasm, "Squeeze-through")
        tick(2)

        assertEquals(0.0, player.experience.get(Skill.Agility))
        assertEquals(Tile(2735, 10008), player.tile)
        assertTrue(player.containsMessage("You need level 62 agility"))
    }
}
