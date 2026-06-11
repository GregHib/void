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

class BakePieTest : WorldTest() {

    @Test
    fun `Bake all pies sequentially`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 65)
        player.levels.set(Skill.Cooking, 34)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("fire_rune", 10)
        player.inventory.add("water_rune", 8)
        player.inventory.add("raw_garden_pie", 2)

        player.interfaceOption("lunar_spellbook", "bake_pie", "Cast")
        tick(10)

        assertEquals(2, player.inventory.count("garden_pie"))
        assertEquals(0, player.inventory.count("raw_garden_pie"))
        assertEquals(120.0, player.experience.get(Skill.Magic))
        assertEquals(276.0, player.experience.get(Skill.Cooking))
    }

    @Test
    fun `Can't bake pies above cooking level`() {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 65)
        player.levels.set(Skill.Cooking, 10)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune")
        player.inventory.add("fire_rune", 5)
        player.inventory.add("water_rune", 4)
        player.inventory.add("raw_garden_pie")

        player.interfaceOption("lunar_spellbook", "bake_pie", "Cast")
        tick(5)

        assertTrue(player.containsMessage("You have no pies which you have the level to bake."))
        assertEquals(1, player.inventory.count("raw_garden_pie"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
