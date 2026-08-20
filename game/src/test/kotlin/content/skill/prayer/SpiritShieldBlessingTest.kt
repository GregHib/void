package content.skill.prayer

import WorldTest
import itemOnObject
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpiritShieldBlessingTest : WorldTest() {

    @Test
    fun `Bless a spirit shield on an altar of Saradomin`() {
        val player = createPlayer(Tile(3244, 3207))
        player.experience.set(Skill.Prayer, Level.experience(85))
        player.inventory.add("spirit_shield")
        player.inventory.add("holy_elixir")

        player.blessOnAltar()

        assertTrue(player.inventory.contains("blessed_spirit_shield"))
        assertFalse(player.inventory.contains("spirit_shield"))
        assertFalse(player.inventory.contains("holy_elixir"))
        assertEquals(Level.experience(85) + 1500.0, player.experience.get(Skill.Prayer))
    }

    @Test
    fun `Blessing a spirit shield needs eighty five prayer`() {
        val player = createPlayer(Tile(3244, 3207))
        player.experience.set(Skill.Prayer, Level.experience(84))
        player.inventory.add("spirit_shield")
        player.inventory.add("holy_elixir")

        player.blessOnAltar()

        assertFalse(player.inventory.contains("blessed_spirit_shield"))
        assertTrue(player.inventory.contains("spirit_shield"))
        assertTrue(player.inventory.contains("holy_elixir"))
    }

    @Test
    fun `Blessing a spirit shield needs a holy elixir`() {
        val player = createPlayer(Tile(3244, 3207))
        player.experience.set(Skill.Prayer, Level.experience(85))
        player.inventory.add("spirit_shield")

        player.blessOnAltar()

        assertFalse(player.inventory.contains("blessed_spirit_shield"))
        assertTrue(player.inventory.contains("spirit_shield"))
    }

    private fun Player.blessOnAltar() {
        val altar = GameObjects.find(Tile(3243, 3206), "prayer_altar_lumbridge")
        itemOnObject(altar, inventory.indexOf("spirit_shield"))
        tick(2)
    }
}
