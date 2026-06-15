package content.skill.magic.book.lunar

import WorldTest
import interfaceOnPlayer
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatSpyTest : WorldTest() {

    @Test
    fun `Spy on another player's stats`() {
        val player = createPlayer(Tile(100, 100))
        val target = createPlayer(Tile(101, 100))
        player.levels.set(Skill.Magic, 75)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("cosmic_rune", 2)
        player.inventory.add("body_rune", 5)

        player.interfaceOnPlayer("lunar_spellbook", "stat_spy", target)
        tick(4)

        assertTrue(player.hasOpen("player_stat_spy"))
        assertTrue(player.hasOpen("lunar_spellbook"))
        assertFalse(player.contains("spell"))
        assertEquals(76.0, player.experience.get(Skill.Magic))

        player.interfaceOption("player_stat_spy", "close", "Close")
        tick(2)

        assertFalse(player.hasOpen("player_stat_spy"))
        assertTrue(player.hasOpen("lunar_spellbook"))
    }
}
