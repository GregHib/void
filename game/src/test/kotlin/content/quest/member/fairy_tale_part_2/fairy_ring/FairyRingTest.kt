package content.quest.member.fairy_tale_part_2.fairy_ring

import WorldTest
import containsMessage
import interfaceOption
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FairyRingTest : WorldTest() {

    @Test
    fun `Outer ring teleports straight to the Zanaris hub`() {
        val player = createPlayer(Tile(3129, 3497))
        player["fairy_tale_ii"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = GameObjects.find(Tile(3129, 3496), "fairy_ring_edgeville")

        player.objectOption(fairyRing, "Use")

        tick(10)
        assertNotEquals("fairy_ring", player.menu)
        assertEquals(Tile(2412, 4434), player.tile)
        assertTrue(player["fairy_rings_unlocked", false])
    }

    @Test
    fun `Lost City completion isn't enough before Fairy Tale II`() {
        val player = createPlayer(Tile(3129, 3497))
        player["lost_city"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = GameObjects.find(Tile(3129, 3496), "fairy_ring_edgeville")

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertTrue(player.containsMessage("You don't have permission to use that fairy ring."))
        assertEquals(Tile(3129, 3497), player.tile)
    }

    @Test
    fun `Quest gated code teleports once the quest is completed`() {
        val player = createPlayer(Tile(2412, 4435))
        player["fairy_tale_ii"] = "completed"
        player["priest_in_peril"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = createObject("fairy_ring_zanaris_2", Tile(2412, 4434))

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertEquals("fairy_ring", player.menu)
        player.interfaceOption("fairy_ring", "clockwise_1", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_1", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_2", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_2", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_3", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "teleport", "Teleport")
        tick(8)

        assertEquals(Tile(3447, 3470), player.tile)
    }

    @Test
    fun `Quest gated code lands beside the hub without the quest`() {
        val player = createPlayer(Tile(2412, 4435))
        player["fairy_tale_ii"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = createObject("fairy_ring_zanaris_2", Tile(2412, 4434))

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertEquals("fairy_ring", player.menu)
        player.interfaceOption("fairy_ring", "clockwise_1", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_1", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_2", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_2", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_3", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "teleport", "Teleport")
        tick(8)

        assertTrue(player.tile.within(Tile(2412, 4434), 2))
    }

    @Test
    fun `Lunar staff counts as fairy magic`() {
        val player = createPlayer(Tile(3129, 3497))
        player["fairy_tale_ii"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "lunar_staff")

        val fairyRing = GameObjects.find(Tile(3129, 3496), "fairy_ring_edgeville")

        player.objectOption(fairyRing, "Use")

        tick(10)
        assertEquals(Tile(2412, 4434), player.tile)
    }

    @Test
    fun `Can't use fairy ring without dramen staff`() {
        val player = createPlayer(Tile(3129, 3497))
        player["fairy_tale_ii"] = "completed"

        val fairyRing = GameObjects.find(Tile(3129, 3496), "fairy_ring_edgeville")

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertNotEquals("fairy_ring", player.menu)
        assertTrue(player.containsMessage("The fairy ring only works for those who wield fairy magic."))
    }

    @Test
    fun `Can't use fairy ring without quest`() {
        val player = createPlayer(Tile(3129, 3497))
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = GameObjects.find(Tile(3129, 3496), "fairy_ring_edgeville")

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertNotEquals("fairy_ring", player.menu)
        assertTrue(player.containsMessage("You don't have permission to use that fairy ring."))
    }

    @Test
    fun `Zanaris ring opens the dial and teleports to the code destination`() {
        val player = createPlayer(Tile(2412, 4435))
        player["fairy_tale_ii"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = createObject("fairy_ring_zanaris_2", Tile(2412, 4434))

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertEquals("fairy_ring", player.menu)
        player.interfaceOption("fairy_ring", "anticlockwise_2", "Rotate anticlockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_3", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "teleport", "Teleport")
        tick(8)

        assertEquals(Tile(2500, 3896), player.tile)
        assertEquals(listOf("ajs"), player.get<MutableList<String>>("travel_log_locations")?.toList())
    }

    @Test
    fun `Zanaris ring without staff after Fairy Tale III`() {
        val player = createPlayer(Tile(2412, 4435))
        player["fairy_tale_ii"] = "completed"
        player["fairy_tale_iii"] = "completed"

        val fairyRing = createObject("fairy_ring_zanaris_2", Tile(2412, 4434))

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertEquals("fairy_ring", player.menu)
        player.interfaceOption("fairy_ring", "anticlockwise_2", "Rotate anticlockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "clockwise_3", "Rotate clockwise")
        tick(1)
        player.interfaceOption("fairy_ring", "teleport", "Teleport")
        tick(8)

        assertEquals(Tile(2500, 3896), player.tile)
    }

    @Test
    fun `Unknown code lands beside the hub`() {
        val player = createPlayer(Tile(2412, 4435))
        player["fairy_tale_ii"] = "completed"
        player.equipment.set(EquipSlot.Weapon.index, "dramen_staff")

        val fairyRing = createObject("fairy_ring_zanaris_2", Tile(2412, 4434))

        player.objectOption(fairyRing, "Use")

        tick(1)
        assertEquals("fairy_ring", player.menu)
        player.interfaceOption("fairy_ring", "teleport", "Teleport")
        tick(8)

        assertTrue(player.tile.within(Tile(2412, 4434), 2))
    }
}
