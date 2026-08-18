package content.skill.firemaking

import WorldTest
import itemOnItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction

internal class FiremakingTest : WorldTest() {

    @Test
    fun `Making a fire removes logs and moves player`() {
        val start = emptyTile
        val player = createPlayer(start)
        player.levels.set(Skill.Firemaking, 100)
        player.inventory.add("tinderbox")
        player.inventory.add("logs", 27)

        player.itemOnItem(0, 2)
        tick(5)

        assertTrue(player.inventory.count("logs") < 27)
        assertTrue(player.inventory[1].isNotEmpty())
        assertTrue(player.inventory[2].isEmpty())
        assertEquals(start.add(Direction.WEST), player.tile)
        assertTrue(player.experience.get(Skill.Firemaking) > 0)
    }

    @Test
    fun `Extinguishing swaps lit candles and torches for their unlit form`() {
        val player = createPlayer(emptyTile)
        player.inventory.add("white_candle_lit")
        player.inventory.add("lit_torch")
        player.inventory.add("candle_lantern_lit_white")

        Light.extinguish(player)

        assertEquals("white_candle", player.inventory[0].id)
        assertEquals("unlit_torch", player.inventory[1].id)
        // Lanterns are deliberately left burning.
        assertEquals("candle_lantern_lit_white", player.inventory[2].id)
    }
}
