package content.skill.agility.shortcut

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.random.Random

class SteppingStonesTest : WorldTest() {

    @Test
    fun `Cross north shilo village waterfall stones`() {
        val player = createPlayer(tile = Tile(2925, 2947))
        player.levels.set(Skill.Agility, 30)

        var stones = objects[Tile(2925, 2948), "shilo_village_waterfall_stepping_stone_south"]!!
        player.objectOption(stones, "Cross")
        tick(4)

        stones = objects[Tile(2925, 2949), "shilo_village_waterfall_stepping_stone_middle"]!!
        player.objectOption(stones, "Cross")
        tick(4)

        stones = objects[Tile(2925, 2950), "shilo_village_waterfall_stepping_stone_north"]!!
        player.objectOption(stones, "Cross")
        tick(4)

        assertEquals(Tile(2925, 2950), player.tile)
        assertEquals(9.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Cross south shilo village waterfall stones`() {
        val player = createPlayer(tile = Tile(2925, 2951))
        player.levels.set(Skill.Agility, 30)

        var stones = objects[Tile(2925, 2950), "shilo_village_waterfall_stepping_stone_north"]!!
        player.objectOption(stones, "Cross")
        tick(4)

        stones = objects[Tile(2925, 2949), "shilo_village_waterfall_stepping_stone_middle"]!!
        player.objectOption(stones, "Cross")
        tick(4)

        stones = objects[Tile(2925, 2948), "shilo_village_waterfall_stepping_stone_south"]!!
        player.objectOption(stones, "Cross")
        tick(4)

        assertEquals(Tile(2925, 2948), player.tile)
        assertEquals(9.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Fail crossing shilo village waterfall stones north`() {
        setRandom(object : Random() {
            override fun nextBits(bitCount: Int): Int = 255
        })
        val player = createPlayer(tile = Tile(2925, 2947))
        player.levels.set(Skill.Agility, 30)
        val stones = objects[Tile(2925, 2948), "shilo_village_waterfall_stepping_stone_south"]!!
        player.objectOption(stones, "Cross")
        tick(13)

        assertEquals(Tile(2931, 2953), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
        assertTrue(player.levels.get(Skill.Constitution) < 100)
    }

    @Test
    fun `Fail crossing shilo village waterfall stones south`() {
        setRandom(object : Random() {
            override fun nextBits(bitCount: Int): Int = 255
        })
        val player = createPlayer(tile = Tile(2925, 2951))
        player.levels.set(Skill.Agility, 30)
        val stones = objects[Tile(2925, 2950), "shilo_village_waterfall_stepping_stone_north"]!!
        player.objectOption(stones, "Cross")
        tick(13)

        assertEquals(Tile(2931, 2945), player.tile)
        assertEquals(1.0, player.experience.get(Skill.Agility))
        assertTrue(player.levels.get(Skill.Constitution) < 100)
    }

    @Test
    fun `Can't cross shilo village waterfall without level`() {
        val player = createPlayer(tile = Tile(2925, 2951))
        player.levels.set(Skill.Agility, 29)
        val stones = objects[Tile(2925, 2950), "shilo_village_waterfall_stepping_stone_north"]!!

        player.objectOption(stones, "Cross")
        tick(2)

        assertTrue(player.containsMessage("You need at least 30"))
    }
}