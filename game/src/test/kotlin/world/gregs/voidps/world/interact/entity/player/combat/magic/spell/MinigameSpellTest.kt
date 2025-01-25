package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import content.skill.magic.spell.removeSpellItems
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class MinigameSpellTest : MagicSpellTest() {

    @Test
    fun `Remove minigame runes`() {
        val player = player()
        setItems(Item("earth_rune", 2), Item("air_rune", 1), Item("death_rune", 1))
        player["minigame_type"] = "fist_of_guthix"

        player.inventory.add("elemental_rune", 10)
        player.inventory.add("catalytic_rune", 10)

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(7, player.inventory.count("elemental_rune"))
        assertEquals(9, player.inventory.count("catalytic_rune"))
    }

    @Test
    fun `Remove last minigame runes`() {
        val player = player()
        setItems(Item("earth_rune", 1), Item("air_rune", 2), Item("death_rune", 1))
        player["minigame_type"] = "stealing_creation"

        player.inventory.add("elemental_rune", 3)
        player.inventory.add("catalytic_rune", 1)

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(0, player.inventory.count("elemental_rune"))
        assertEquals(0, player.inventory.count("catalytic_rune"))
    }

    @Test
    fun `Can't use minigame runes outside of minigame`() {
        val player = player()
        setItems(Item("earth_rune", 1), Item("air_rune", 2), Item("death_rune", 1))

        player.inventory.add("elemental_rune", 10)
        player.inventory.add("catalytic_rune", 10)

        assertFalse(player.removeSpellItems("spell"))
        assertEquals(10, player.inventory.count("elemental_rune"))
        assertEquals(10, player.inventory.count("catalytic_rune"))
    }

    @Test
    fun `Not enough minigame runes`() {
        val player = player()
        setItems(Item("air_rune", 3), Item("death_rune", 1))
        player["minigame_type"] = "barbarian_assault"

        player.inventory.add("elemental_rune", 2)
        player.inventory.add("catalytic_rune", 1)

        assertFalse(player.removeSpellItems("spell"))
        assertEquals(2, player.inventory.count("elemental_rune"))
        assertEquals(1, player.inventory.count("catalytic_rune"))
    }

}