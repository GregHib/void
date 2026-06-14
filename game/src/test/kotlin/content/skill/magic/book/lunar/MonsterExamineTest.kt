package content.skill.magic.book.lunar

import WorldTest
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.instruction.handle.interactOn
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MonsterExamineTest : WorldTest() {

    @Test
    fun `Examine a monster opens the stat interface without attacking`() {
        val player = createPlayer(Tile(100, 100))
        player.levels.set(Skill.Magic, 66)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("mind_rune")
        player.inventory.add("cosmic_rune")
        val npc = createNPC("rat", Tile(101, 100))

        player.interactOn(npc, "lunar_spellbook", "monster_examine")
        tick(4)

        assertTrue(player.hasOpen("monster_stat_spy"))
        assertTrue(player.hasOpen("lunar_spellbook"))
        assertFalse(player.contains("spell"))
        assertEquals(66.0, player.experience.get(Skill.Magic))

        player.interfaceOption("monster_stat_spy", "close", "Close")
        tick(2)

        assertFalse(player.hasOpen("monster_stat_spy"))
        assertTrue(player.hasOpen("lunar_spellbook"))
    }
}
