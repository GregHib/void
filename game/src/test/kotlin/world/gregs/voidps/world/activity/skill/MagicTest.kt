package world.gregs.voidps.world.activity.skill

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.script.WorldTest
import world.gregs.voidps.world.script.interfaceOption

internal class MagicTest : WorldTest() {

    @Test
    fun `Teleport to another place`() {
        val tile = emptyTile
        val player = createPlayer("magician", tile)
        player.experience.set(Skill.Magic, experience)
        player.inventory.add("law_rune")
        player.inventory.add("air_rune", 3)
        player.inventory.add("fire_rune")

        player.interfaceOption("modern_spellbook", "varrock_teleport", "Cast")
        tickIf { player.tile == tile }

        assertTrue(player.inventory.isEmpty())
        assertTrue(player.experience.get(Skill.Magic) > experience)
        assertNotEquals(tile, player.tile)
    }

    @Test
    fun `Teleport with a tablet`() {
        val tile = emptyTile
        val player = createPlayer("magician", tile)
        player.experience.set(Skill.Magic, experience)
        player.inventory.add("lumbridge_teleport")

        player.interfaceOption("inventory", "container", "Break", 0, Item("lumbridge_teleport"), 0)
        tick(5)

        assertTrue(player.inventory.isEmpty())
        assertFalse(player.experience.get(Skill.Magic) > experience)
        assertNotEquals(tile, player.tile)
    }

    companion object {
        private const val experience = 14000000.0
    }
}