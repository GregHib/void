package content.skill.magic.book.modern

import WorldTest
import containsMessage
import interfaceOnItem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import kotlin.test.assertTrue

class AlchemyTest : WorldTest() {
    @ParameterizedTest
    @ValueSource(strings = ["low", "high"])
    fun `Alch an item`(level: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 55)
        player.inventory.add("fire_rune", 5)
        player.inventory.add("nature_rune")
        player.inventory.add("adamant_sword")

        player.interfaceOnItem("modern_spellbook", "${level}_level_alchemy", Item("adamant_sword"), 2)
        tick(2)

        assertEquals(0, player.inventory.count("scimitar"))
        assertEquals(0, player.inventory.count("nature_rune"))
        if (level == "low") {
            assertEquals(2, player.inventory.count("fire_rune"))
            assertEquals(832, player.inventory.count("coins"))
            assertEquals(31.0, player.experience.get(Skill.Magic))
        } else {
            assertEquals(0, player.inventory.count("fire_rune"))
            assertEquals(1248, player.inventory.count("coins"))
            assertEquals(65.0, player.experience.get(Skill.Magic))
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["low", "high"])
    fun `Expensive item gives warning`(level: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 55)
        player.inventory.add("fire_rune", 5)
        player.inventory.add("nature_rune")
        player.inventory.add("abyssal_whip")

        player.interfaceOnItem("modern_spellbook", "${level}_level_alchemy", Item("abyssal_whip"), 2)
        tick(4)
        assertNotNull(player.dialogue)
    }

    @ParameterizedTest
    @ValueSource(strings = ["low", "high"])
    fun `Can't alch destructible items`(level: String) {
        val player = createPlayer()
        player.levels.set(Skill.Magic, 55)
        player.inventory.add("fire_rune", 5)
        player.inventory.add("nature_rune")
        player.inventory.add("silverlight")

        player.interfaceOnItem("modern_spellbook", "${level}_level_alchemy", Item("silverlight"), 2)
        tick(2)
        assertEquals(1, player.inventory.count("silverlight"))
        assertEquals(5, player.inventory.count("fire_rune"))
        assertEquals(1, player.inventory.count("nature_rune"))
        assertEquals(0, player.inventory.count("coins"))
        assertTrue(player.containsMessage("This spell can not be cast"))
    }
}
