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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DreamTest : WorldTest() {

    @Test
    fun `Dream heals until hitpoints are full`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 79)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("cosmic_rune")
        player.inventory.add("body_rune", 5)
        player.levels.set(Skill.Constitution, 80)

        player.interfaceOption("lunar_spellbook", "dream", "Cast")
        tick(3)

        assertTrue(player["dream", false])
        assertEquals(82.0, player.experience.get(Skill.Magic))

        tick(30)
        assertTrue(player.levels.get(Skill.Constitution) > 80)
    }

    @Test
    fun `Can't dream at full hitpoints`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 79)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("cosmic_rune")
        player.inventory.add("body_rune", 5)

        player.interfaceOption("lunar_spellbook", "dream", "Cast")
        tick(2)

        assertTrue(player.containsMessage("You have no need to cast this spell since your hitpoints are already full."))
        assertFalse(player["dream", false])
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
