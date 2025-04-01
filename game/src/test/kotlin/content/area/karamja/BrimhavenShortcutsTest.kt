package content.area.karamja

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrimhavenShortcutsTest : WorldTest() {

    @Test
    fun `Swing to brimhaven island`() {
        val player = createPlayer(tile = Tile(2709, 3209))
        player.levels.set(Skill.Agility, 10)
        val ropeswing = objects[Tile(2705, 3209), "brimhaven_ropeswing_west"]!!

        player.objectOption(ropeswing, "Swing-on")
        tick(3)

        assertEquals(Tile(2704, 3209), player.tile)
        assertEquals(3.0, player.experience.get(Skill.Agility))
    }

    @Test
    fun `Can't swing to brimhaven island without agility level`() {
        val player = createPlayer(tile = Tile(2709, 3209))
        val ropeswing = objects[Tile(2705, 3209), "brimhaven_ropeswing_west"]!!

        player.objectOption(ropeswing, "Swing-on")
        tick(2)

        assertTrue(player.containsMessage("You need an agility level of 10"))
    }

    @Test
    fun `Swing from brimhaven island`() {
        val player = createPlayer(tile = Tile(2705, 3205))
        player.levels.set(Skill.Agility, 10)
        val ropeSwing = objects[Tile(2703, 3205), "brimhaven_ropeswing_east"]!!

        player.objectOption(ropeSwing, "Swing-on")
        tick(3)

        assertEquals(Tile(2709, 3205), player.tile)
        assertEquals(3.0, player.experience.get(Skill.Agility))
    }
}