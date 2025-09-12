package content.quest.member.fairy_tale_part_2.fairy_ring

import WorldTest
import containsMessage
import interfaceOption
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FairyRingTest : WorldTest() {

    @Test
    fun `Use fairy ring`() {
        val player = createPlayer(Tile(3129, 3497))
        player["fairy_tale_ii"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = objects[Tile(3129, 3496), "fairy_ring_edgeville"]!!

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertEquals("fairy_ring", player.menu)
        player.interfaceOption("fairy_ring", "clockwise_3", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "teleport", "Teleport")
        tick(6)

        assertEquals(Tile(2500, 3896), player.tile)
    }

    @Test
    fun `Can't use fairy ring without dramen staff`() {
        val player = createPlayer(Tile(3129, 3497))
        player["fairy_tale_ii"] = "completed"

        val fairyRing = objects[Tile(3129, 3496), "fairy_ring_edgeville"]!!

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertNotEquals("fairy_ring", player.menu)
        assertTrue(player.containsMessage("The fairy ring only works for those who wield fairy magic."))
    }

    @Test
    fun `Can't use fairy ring without quest`() {
        val player = createPlayer(Tile(3129, 3497))
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = objects[Tile(3129, 3496), "fairy_ring_edgeville"]!!

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertNotEquals("fairy_ring", player.menu)
        assertTrue(player.containsMessage("You don't have permission to use that fairy ring."))
    }

    @Test
    fun `Use fairy ring without dramen staff`() {
        val player = createPlayer(Tile(3129, 3497))
        player["fairy_tale_ii"] = "completed"
        player["fairy_tale_iii"] = "completed"

        val fairyRing = objects[Tile(3129, 3496), "fairy_ring_edgeville"]!!

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertEquals("fairy_ring", player.menu)
        player.interfaceOption("fairy_ring", "clockwise_3", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "teleport", "Teleport")
        tick(6)

        assertEquals(Tile(2500, 3896), player.tile)
    }
}
