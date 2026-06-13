package content.skill.magic.book.lunar

import WorldTest
import containsMessage
import interfaceOnItem
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlankMakeTest : WorldTest() {

    @Test
    fun `Convert logs into a plank`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 86)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("nature_rune")
        player.inventory.add("earth_rune", 15)
        player.inventory.add("coins", 175)
        player.inventory.add("oak_logs")

        player.interfaceOnItem("lunar_spellbook", "plank_make", Item("oak_logs"), player.inventory.indexOf("oak_logs"))
        tick(4)

        assertEquals(1, player.inventory.count("oak_plank"))
        assertEquals(0, player.inventory.count("oak_logs"))
        assertEquals(0, player.inventory.count("coins"))
        assertEquals(90.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't convert without enough coins`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 86)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("nature_rune")
        player.inventory.add("earth_rune", 15)
        player.inventory.add("coins", 50)
        player.inventory.add("oak_logs")

        player.interfaceOnItem("lunar_spellbook", "plank_make", Item("oak_logs"), player.inventory.indexOf("oak_logs"))
        tick(4)

        assertTrue(player.containsMessage("You need 175 coins to convert those logs into planks."))
        assertEquals(1, player.inventory.count("oak_logs"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }

    @Test
    fun `Can't cast on non-logs`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 86)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("nature_rune")
        player.inventory.add("earth_rune", 15)
        player.inventory.add("coins", 1050)
        player.inventory.add("bones")

        player.interfaceOnItem("lunar_spellbook", "plank_make", Item("bones"), player.inventory.indexOf("bones"))
        tick(4)

        assertTrue(player.containsMessage("You need to cast this spell on logs."))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
