package content.skill.crafting

import WorldTest
import containsMessage
import interfaceOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ButtonPolishingTest : WorldTest() {

    @Test
    fun `Buttons polish at level 3 crafting`() {
        val player = createPlayer(name = "player")
        player.levels.set(Skill.Crafting, 3)
        player.inventory.add("buttons")

        player.interfaceOption("inventory", "inventory", "Option1", 0, Item("buttons"), 0)
        tick()

        assertEquals(0, player.inventory.count("buttons"))
        assertEquals(1, player.inventory.count("polished_buttons"))
        assertEquals(5.0, player.experience.get(Skill.Crafting))
        assertTrue(player.containsMessage("You rub the buttons on your clothes and they become more shiny."))
    }

    @Test
    fun `Buttons do not polish below level 3 crafting`() {
        val player = createPlayer(name = "player")
        player.levels.set(Skill.Crafting, 2)
        player.inventory.add("buttons")

        player.interfaceOption("inventory", "inventory", "Option1", 0, Item("buttons"), 0)
        tick()

        assertEquals(1, player.inventory.count("buttons"))
        assertEquals(0, player.inventory.count("polished_buttons"))
        assertEquals(0.0, player.experience.get(Skill.Crafting))
        assertTrue(player.containsMessage("You rub the buttons on your clothes but they aren't improved by the process"))
    }
}
