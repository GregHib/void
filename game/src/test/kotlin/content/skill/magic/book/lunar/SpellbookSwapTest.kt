package content.skill.magic.book.lunar

import WorldTest
import dialogueOption
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.hasOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpellbookSwapTest : WorldTest() {

    private fun cast(player: Player) {
        player.levels.set(Skill.Magic, 96)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 3)
        player.inventory.add("cosmic_rune", 2)
        player.inventory.add("law_rune")
        player.interfaceOption("lunar_spellbook", "spellbook_swap", "Cast")
        tick(2)
        player.dialogueOption("line2")
        tick(3)
    }

    @Test
    fun `Swap to the modern spellbook then revert after one cast`() {
        val player = createPlayer()
        cast(player)

        assertTrue(player.hasOpen("modern_spellbook"))
        assertEquals(130.0, player.experience.get(Skill.Magic))

        player.inventory.add("nature_rune")
        player.inventory.add("earth_rune", 2)
        player.inventory.add("water_rune", 2)
        player.inventory.add("bones")
        player.interfaceOption("modern_spellbook", "bones_to_bananas", "Cast")
        tick(4)

        assertEquals(1, player.inventory.count("banana"))
        assertTrue(player.hasOpen("lunar_spellbook"))
        assertFalse(player.contains("spellbook_swap"))
    }

    @Test
    fun `Swap reverts when the timer runs out`() {
        val player = createPlayer()
        cast(player)

        assertTrue(player.hasOpen("modern_spellbook"))
        tick(205)
        assertTrue(player.hasOpen("lunar_spellbook"))
        assertFalse(player.contains("spellbook_swap"))
    }
}
