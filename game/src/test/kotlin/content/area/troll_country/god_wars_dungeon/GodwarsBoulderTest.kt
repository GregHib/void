package content.area.troll_country.god_wars_dungeon

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GodwarsBoulderTest : WorldTest() {

    @Test
    fun `Move boulder out of the way`() {
        val player = createPlayer(Tile(2898, 3715))
        player.levels.set(Skill.Strength, 60)

        val hole = objects[Tile(2898, 3716), "godwars_boulder"]!!

        player.objectOption(hole, optionIndex = 0) // Move
        tick(8)
        assertEquals(Tile(2898, 3719), player.tile)
    }

    @Test
    fun `Move boulder out of the way reversed`() {
        val player = createPlayer(Tile(2898, 3719))
        player.levels.set(Skill.Strength, 60)

        val hole = objects[Tile(2898, 3716), "godwars_boulder"]!!

        player.objectOption(hole, optionIndex = 0) // Move
        tick(8)
        assertEquals(Tile(2898, 3715), player.tile)
    }

    @Test
    fun `Crawl through gap`() {
        val player = createPlayer(Tile(2899, 3713))
        player.levels.set(Skill.Agility, 60)

        val hole = objects[Tile(2900, 3713), "godwars_little_hole"]!!

        player.objectOption(hole, "Crawl-through")
        tick(8)
        assertEquals(Tile(2904, 3720), player.tile)
    }

    @Test
    fun `Crawl through gap reversed`() {
        val player = createPlayer(Tile(2904, 3720))
        player.levels.set(Skill.Agility, 60)

        val hole = objects[Tile(2904, 3719), "godwars_little_hole"]!!

        player.objectOption(hole, "Crawl-through")
        tick(8)
        assertEquals(Tile(2899, 3713), player.tile)
    }

    @Test
    fun `Can't move boulder without 60 strength`() {
        val player = createPlayer(Tile(2898, 3715))
        player.levels.set(Skill.Strength, 59)

        val hole = objects[Tile(2898, 3716), "godwars_boulder"]!!

        player.objectOption(hole, optionIndex = 0) // Move
        tick(2)
        assertTrue(player.containsMessage("You need to have a Strength level of 60"))
    }

    @Test
    fun `Can't crawl through gap without 60 agility`() {
        val player = createPlayer(Tile(2899, 3713))
        player.levels.set(Skill.Agility, 59)

        val hole = objects[Tile(2900, 3713), "godwars_little_hole"]!!

        player.objectOption(hole, "Crawl-through")
        tick()
        assertTrue(player.containsMessage("You need to have an Agility level of 60"))
    }
}
