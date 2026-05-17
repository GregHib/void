package content.area.wilderness.daemonheim

import WorldTest
import interfaceOption
import itemOption
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class RingOfKinshipTest : WorldTest() {

    @Test
    fun `Quick switch class in dungeoneering`() {
        val player = createPlayer(Tile(3449, 3725))
        player["in_dungeoneering"] = true
        player["kinship_quick_switch_class"] = "tank"
        player.inventory.add("ring_of_kinship_tactician")

        player.itemOption("Quick-switch", "ring_of_kinship_tactician")

        assertFalse(player.inventory.contains("ring_of_kinship_tactician"))
        assertTrue(player.inventory.contains("ring_of_kinship_tank"))
        assertEquals("tank", player["kinship_class", ""])
        assertEquals("tactician", player["kinship_quick_switch_class", ""])
    }

    @Test
    fun `Switch classes in dungeoneering`() {
        val player = createPlayer(Tile(3449, 3725))
        player["in_dungeoneering"] = true
        player["kinship_quick_switch_class"] = "tank"
        player.inventory.add("ring_of_kinship")

        player.itemOption("Customise", "ring_of_kinship")
        player.interfaceOption("kinship_customisation", "switch_1", "Switch-to")

        assertFalse(player.inventory.contains("ring_of_kinship"))
        assertTrue(player.inventory.contains("ring_of_kinship_tank"))
        assertEquals("tank", player["kinship_class", ""])
    }

    @Test
    fun `Upgrade class in dungeoneering`() {
        val player = createPlayer(Tile(3449, 3725))
        player["in_dungeoneering"] = true
        player["dungeoneering_tokens"] = 1000
        player["kinship_class"] = "tank"
        player.inventory.add("ring_of_kinship_tank")

        player.itemOption("Customise", "ring_of_kinship_tank")
        player.interfaceOption("kinship_customisation", "upgrade_1", "Upgrade")
        player.interfaceOption("kinship_customisation", "upgrade_confirm", "Confirm")

        assertEquals(1, player["kinship_tank_level", 0])
        assertEquals(865, player["dungeoneering_tokens", 0])
    }
}
