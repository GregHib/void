package content.quest.member.ghost_ahoy

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EctopoolTest : WorldTest() {

    @Test
    fun `Jump down shortcut`() {
        val player = createPlayer(Tile(3670, 9888, 3))
        player.levels.set(Skill.Agility, 58)
        val rail = objects[Tile(3670, 9888, 3), "ectopool_shortcut_rail"]!!

        player.objectOption(rail, "Jump-down")
        tick(4)

        assertEquals(Tile(3671, 9888, 2), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't jump down shortcut without levels`() {
        val player = createPlayer(Tile(3670, 9888, 3))
        player.levels.set(Skill.Agility, 57)
        val rail = objects[Tile(3670, 9888, 3), "ectopool_shortcut_rail"]!!

        player.objectOption(rail, "Jump-down")
        tick(2)

        assertTrue(player.containsMessage("You need an agility level of at least 58"))
    }

    @Test
    fun `Jump up shortcut`() {
        val player = createPlayer(Tile(3671, 9888, 2))
        player.levels.set(Skill.Agility, 58)
        val rail = objects[Tile(3670, 9888, 2), "ectopool_shortcut_wall"]!!

        player.objectOption(rail, "Jump-up")
        tick(4)

        assertEquals(Tile(3670, 9888, 3), player.tile)
        assertEquals(0.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't jump up shortcut without levels`() {
        val player = createPlayer(Tile(3671, 9888, 2))
        player.levels.set(Skill.Agility, 57)
        val rail = objects[Tile(3670, 9888, 2), "ectopool_shortcut_wall"]!!

        player.objectOption(rail, "Jump-up")
        tick(2)

        assertTrue(player.containsMessage("You need an agility level of at least 58"))
    }
}