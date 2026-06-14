package content.skill.magic.book.lunar

import WorldTest
import containsMessage
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringJewelleryTest : WorldTest() {

    @Test
    fun `String all unstrung amulets sequentially`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 80)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 4)
        player.inventory.add("earth_rune", 20)
        player.inventory.add("water_rune", 10)
        player.inventory.add("gold_amulet_unstrung", 2)

        player.interfaceOption("lunar_spellbook", "string_jewellery", "Cast")
        tick(10)

        assertEquals(2, player.inventory.count("gold_amulet"))
        assertEquals(0, player.inventory.count("gold_amulet_unstrung"))
        assertEquals(166.0, player.experience.get(Skill.Magic))
        assertEquals(8.0, player.experience.get(Skill.Crafting))
    }

    @Test
    fun `Can't cast with nothing to string`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 80)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("earth_rune", 10)
        player.inventory.add("water_rune", 5)

        player.interfaceOption("lunar_spellbook", "string_jewellery", "Cast")
        tick(3)

        assertTrue(player.containsMessage("You have no unstrung items to cast this spell on."))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
