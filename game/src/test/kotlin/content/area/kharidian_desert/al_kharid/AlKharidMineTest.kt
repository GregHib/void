package content.area.kharidian_desert.al_kharid

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlKharidMineTest : WorldTest() {

    @Test
    fun `Climb up mine shortcut`() {
        val player = createPlayer(Tile(3303, 3315))
        player.levels.set(Skill.Agility, 38)
        val rocks = objects.find(Tile(3304, 3315), "al_kharid_mine_shortcut_bottom")
        player.objectOption(rocks, "Climb")
        tick(5)

        assertEquals(Tile(3307, 3315), player.tile)
    }

    @Test
    fun `Can't climb up mine shortcut without levels`() {
        val player = createPlayer(Tile(3303, 3315))
        player.levels.set(Skill.Agility, 37)
        val rocks = objects.find(Tile(3304, 3315), "al_kharid_mine_shortcut_bottom")
        player.objectOption(rocks, "Climb")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 38"))
    }

    @Test
    fun `Climb down mine shortcut`() {
        val player = createPlayer(Tile(3307, 3315))
        player.levels.set(Skill.Agility, 38)
        val rocks = objects.find(Tile(3306, 3315), "al_kharid_mine_shortcut_top")
        player.objectOption(rocks, "Climb")
        tick(5)

        assertEquals(Tile(3303, 3315), player.tile)
    }

    @Test
    fun `Can't climb down mine shortcut without levels`() {
        val player = createPlayer(Tile(3307, 3315))
        player.levels.set(Skill.Agility, 37)
        val rocks = objects.find(Tile(3306, 3315), "al_kharid_mine_shortcut_top")
        player.objectOption(rocks, "Climb")
        tick(2)

        assertTrue(player.containsMessage("You need an Agility level of 38"))
    }
}
