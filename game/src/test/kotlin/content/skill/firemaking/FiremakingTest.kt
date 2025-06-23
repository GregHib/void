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
        tickIf { player.tile == start }

        assertTrue(player.inventory.count("logs") < 27)
        assertTrue(player.inventory[1].isNotEmpty())
        assertTrue(player.inventory[2].isEmpty())
        assertEquals(start.add(Direction.WEST), player.tile)
        assertTrue(player.experience.get(Skill.Firemaking) > 0)
    }
}
