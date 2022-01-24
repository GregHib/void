package world.gregs.voidps.world.activity.skill

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.itemOnItem

internal class FiremakingTest : WorldTest() {

    @Test
    fun `Making a fire removes logs and moves player`() {
        val start = emptyTile
        val player = createPlayer("arsonist", start)
        player.levels.setOffset(Skill.Firemaking, 100)
        player.inventory.add("tinderbox")
        player.inventory.add("logs", 27)

        player.itemOnItem(0, 2)
        tickIf { player.tile == start }

        assertTrue(player.inventory.getCount("logs") < 27)
        assertTrue(player.inventory.getItem(1).isNotEmpty())
        assertTrue(player.inventory.getItem(2).isEmpty())
        assertTrue(player.tile == start.add(Direction.WEST))
        assertTrue(player.experience.get(Skill.Firemaking) > 0)
    }

}