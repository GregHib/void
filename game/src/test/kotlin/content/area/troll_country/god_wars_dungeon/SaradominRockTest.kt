package content.area.troll_country.god_wars_dungeon

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaradominRockTest : WorldTest() {

    @Test
    fun `Tie rope on top rock and climb down`() {
        val player = createPlayer(Tile(2912, 5300, 2))
        player.inventory.add("rope")
        player.levels.set(Skill.Agility, 70)
        val rock = objects[Tile(2913, 5300, 2), "godwars_saradomin_rock_top_base"]!!

        player.objectOption(rock, optionIndex = 0) // Tie-rope
        tick(2)
        assertFalse(player.inventory.contains("rope"))
        player.objectOption(rock, optionIndex = 1) // Climb-down
        tick(5)

        assertEquals(Tile(2915, 5300, 1), player.tile)
    }

    @Test
    fun `Tie rope on bottom rock and climb down`() {
        val player = createPlayer(Tile(2920, 5276, 1))
        player.inventory.add("rope")
        player.levels.set(Skill.Agility, 70)
        val rock = objects[Tile(2920, 5274, 1), "godwars_saradomin_rock_bottom_base"]!!

        player.objectOption(rock, optionIndex = 0) // Tie-rope
        tick(2)
        assertFalse(player.inventory.contains("rope"))
        player.objectOption(rock, optionIndex = 1) // Climb-down
        tick(5)

        assertEquals(Tile(2919, 5274), player.tile)
    }

    @Test
    fun `Can't tie rope on top rock without a rope`() {
        val player = createPlayer(Tile(2912, 5300, 2))
        player.levels.set(Skill.Agility, 70)
        val rock = objects[Tile(2913, 5300, 2), "godwars_saradomin_rock_top_base"]!!

        player.objectOption(rock, "Tie-rope", optionIndex = 0)
        tick(2)

        assertTrue(player.containsMessage("You aren't carrying a rope"))
    }

    @Test
    fun `Can't tie rope on bottom rock without a rope`() {
        val player = createPlayer(Tile(2920, 5276, 1))
        player.levels.set(Skill.Agility, 70)
        val rock = objects[Tile(2920, 5274, 1), "godwars_saradomin_rock_bottom_base"]!!

        player.objectOption(rock, "Tie-rope", optionIndex = 0)
        tick(2)

        assertTrue(player.containsMessage("You aren't carrying a rope"))
    }

    @Test
    fun `Can't tie rope on top rock without 70 agility`() {
        val player = createPlayer(Tile(2912, 5300, 2))
        player.inventory.add("rope")
        val rock = objects[Tile(2913, 5300, 2), "godwars_saradomin_rock_top_base"]!!

        player.objectOption(rock, "Tie-rope", optionIndex = 0)
        tick(2)

        assertTrue(player.containsMessage("You need to have an Agility level of 70"))
    }

    @Test
    fun `Can't tie rope on bottom rock without 70 agility`() {
        val player = createPlayer(Tile(2920, 5276, 1))
        player.inventory.add("rope")
        val rock = objects[Tile(2920, 5274, 1), "godwars_saradomin_rock_bottom_base"]!!

        player.objectOption(rock, "Tie-rope", optionIndex = 0)
        tick(2)

        assertTrue(player.containsMessage("You need to have an Agility level of 70"))
    }

    @Test
    fun `Climb up top rock`() {
        val player = createPlayer(Tile(2915, 5300, 1))
        player.inventory.add("rope")
        val rock = objects[Tile(2914, 5300, 1), "godwars_saradomin_rope_top_end_base"]!!

        player["godwars_saradomin_rope_top"] = true
        player.objectOption(rock, optionIndex = 0) // Climb-up
        tick(4)

        assertEquals(Tile(2912, 5300, 2), player.tile)
    }

    @Test
    fun `Climb up bottom rock`() {
        val player = createPlayer(Tile(2919, 5274))
        player.inventory.add("rope")
        val rock = objects[Tile(2920, 5274), "godwars_saradomin_rope_bottom_end_base"]!!

        player["godwars_saradomin_rope_bottom"] = true
        player.objectOption(rock, optionIndex = 0) // Climb-up
        tick(4)

        assertEquals(Tile(2920, 5276, 1), player.tile)
    }
}
