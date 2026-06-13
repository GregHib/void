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
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PotionShareTest : WorldTest() {

    @Test
    fun `Share a restore potion with an adjacent player`() {
        val player = createPlayer(Tile(100, 100))
        val target = createPlayer(Tile(101, 100))
        player.levels.set(Skill.Magic, 81)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 2)
        player.inventory.add("earth_rune", 10)
        player.inventory.add("water_rune", 10)
        player.inventory.add("prayer_potion_4")
        player.levels.set(Skill.Prayer, 10)
        player.levels.drain(Skill.Prayer, 20)
        target.levels.set(Skill.Prayer, 10)
        target.levels.drain(Skill.Prayer, 20)

        player.interfaceOnItem("lunar_spellbook", "stat_restore_pot_share", Item("prayer_potion_4"), player.inventory.indexOf("prayer_potion_4"))
        tick(4)

        assertEquals(1, player.inventory.count("prayer_potion_2"))
        assertEquals(84.0, player.experience.get(Skill.Magic))
        assertTrue(player.levels.get(Skill.Prayer) > 0)
        assertTrue(target.levels.get(Skill.Prayer) > 0)
    }

    @Test
    fun `Can't share with nobody around`() {
        val player = createPlayer(Tile(200, 200))
        player.levels.set(Skill.Magic, 84)
        player.open("lunar_spellbook")
        player.inventory.add("astral_rune", 3)
        player.inventory.add("earth_rune", 12)
        player.inventory.add("water_rune", 10)
        player.inventory.add("attack_potion_4")

        player.interfaceOnItem("lunar_spellbook", "boost_potion_share", Item("attack_potion_4"), player.inventory.indexOf("attack_potion_4"))
        tick(4)

        assertTrue(player.containsMessage("There is nobody around to share the potion with."))
        assertEquals(1, player.inventory.count("attack_potion_4"))
        assertEquals(0.0, player.experience.get(Skill.Magic))
    }
}
