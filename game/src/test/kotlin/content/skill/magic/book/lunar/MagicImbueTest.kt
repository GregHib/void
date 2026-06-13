package content.skill.magic.book.lunar

import WorldTest
import containsMessage
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MagicImbueTest : WorldTest() {

    @Test
    fun `Cast magic imbue`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 82)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("fire_rune", 7)
        player.inventory.add("water_rune", 7)

        player.interfaceOption("lunar_spellbook", "magic_imbue", "Cast")
        tick(2)

        assertTrue(player.hasClock("magic_imbue"))
        assertTrue(player.containsMessage("You are charged to combine runes!"))
        assertEquals(86.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't cast while already imbued`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 82)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 4)
        player.inventory.add("fire_rune", 14)
        player.inventory.add("water_rune", 14)

        player.interfaceOption("lunar_spellbook", "magic_imbue", "Cast")
        tick(2)
        player.interfaceOption("lunar_spellbook", "magic_imbue", "Cast")
        tick(2)

        assertTrue(player.containsMessage("You are already charged to combine runes!"))
        assertEquals(2, player.inventory.count("astral_rune"))
    }
}
