package content.area.kandarin.seers_village

import WorldTest
import containsMessage
import itemOnObject
import objectOption
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoalTrucksTest : WorldTest() {

    @Test
    fun `Coal can be added to a truck`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player.inventory.add("coal", 10)

        player.itemOnObject(truck, player.inventory.indexOf("coal"))
        tick()

        assertEquals(0, player.inventory.count("coal"))
        assertEquals(10, player["coal_truck_coal_count", 0])
        assertEquals(1, player["coal_truck_coal", 0])
        assertTrue(player.containsMessage("You put the coal in the truck."))
    }

    @Test
    fun `Coal truck can be investigated`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player["coal_truck_coal_count"] = 10

        player.objectOption(truck, "Investigate")
        tick()

        assertTrue(player.containsMessage("The truck contains 10 pieces of coal."))
    }

    @Test
    fun `Coal can be removed from a truck`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck_with_some_coal", emptyTile.addY(1))
        player["coal_truck_coal_count"] = 29
        player["coal_truck_coal"] = 1

        player.objectOption(truck, "Remove-coal")
        tick()

        assertEquals(28, player.inventory.count("coal"))
        assertEquals(1, player["coal_truck_coal_count", 0])
        assertEquals(1, player["coal_truck_coal", 0])
        assertTrue(player.containsMessage("You remove some of the coal from the truck."))
    }

    @Test
    fun `Removing the last coal empties the truck`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck_with_some_coal", emptyTile.addY(1))
        player["coal_truck_coal_count"] = 10
        player["coal_truck_coal"] = 1

        player.objectOption(truck, "Remove-coal")
        tick()

        assertEquals(10, player.inventory.count("coal"))
        assertEquals(0, player["coal_truck_coal_count", 0])
        assertEquals(0, player["coal_truck_coal", 0])
        assertTrue(player.containsMessage("The truck is now empty."))
    }

    @Test
    fun `Coal truck reports when full`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player["coal_truck_coal_count"] = 120
        player["coal_truck_coal"] = 140
        player.inventory.add("coal")

        player.itemOnObject(truck, player.inventory.indexOf("coal"))
        tick()

        assertEquals(1, player.inventory.count("coal"))
        assertTrue(player.containsMessage("The coal truck is full."))
    }

    @Test
    fun `Coal truck migrates legacy count into full display state`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player["coal_truck_coal"] = 120

        player.objectOption(truck, "Investigate")
        tick()

        assertEquals(120, player["coal_truck_coal_count", 0])
        assertEquals(140, player["coal_truck_coal", 0])
        assertTrue(player.containsMessage("The truck contains 120 pieces of coal."))
    }

    @Test
    fun `Coal truck migrates display-only full state to 120 coal`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player["coal_truck_coal"] = 140

        player.objectOption(truck, "Investigate")
        tick()

        assertEquals(120, player["coal_truck_coal_count", 0])
        assertEquals(140, player["coal_truck_coal", 0])
        assertTrue(player.containsMessage("The truck contains 120 pieces of coal."))
    }

    @Test
    fun `Coal truck stays nearly full until capacity`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player["coal_truck_coal_count"] = 119

        player.objectOption(truck, "Investigate")
        tick()

        assertEquals(80, player["coal_truck_coal", 0])
        assertTrue(player.containsMessage("The truck contains 119 pieces of coal."))
    }

    @Test
    fun `Coal truck preserves higher storage while showing full`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player["coal_truck_coal_count"] = 196

        player.objectOption(truck, "Investigate")
        tick()

        assertEquals(196, player["coal_truck_coal_count", 0])
        assertEquals(140, player["coal_truck_coal", 0])
        assertTrue(player.containsMessage("The truck contains 196 pieces of coal."))
    }

    @Test
    fun `Coal truck higher storage still blocks adding coal`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player["coal_truck_coal_count"] = 196
        player.inventory.add("coal")

        player.itemOnObject(truck, player.inventory.indexOf("coal"))
        tick()

        assertEquals(196, player["coal_truck_coal_count", 0])
        assertEquals(140, player["coal_truck_coal", 0])
        assertEquals(1, player.inventory.count("coal"))
        assertTrue(player.containsMessage("The coal truck is full."))
    }

    @Test
    fun `Coal truck requires seers headband 1 for 140 coal`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player.equipment.set(EquipSlot.Hat.index, "seers_headband_1")
        player["coal_truck_coal_count"] = 139
        player.inventory.add("coal")

        player.itemOnObject(truck, player.inventory.indexOf("coal"))
        tick()

        assertEquals(140, player["coal_truck_coal_count", 0])
        assertEquals(140, player["coal_truck_coal", 0])
        assertEquals(0, player.inventory.count("coal"))
        assertTrue(player.containsMessage("The coal truck is now full."))
    }

    @Test
    fun `Coal truck requires seers headband 2 for 168 coal`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player.equipment.set(EquipSlot.Hat.index, "seers_headband_2")
        player["coal_truck_coal_count"] = 167
        player.inventory.add("coal")

        player.itemOnObject(truck, player.inventory.indexOf("coal"))
        tick()

        assertEquals(168, player["coal_truck_coal_count", 0])
        assertEquals(140, player["coal_truck_coal", 0])
        assertEquals(0, player.inventory.count("coal"))
        assertTrue(player.containsMessage("The coal truck is now full."))
    }

    @Test
    fun `Coal truck requires seers headband 3 for 196 coal`() {
        val player = createPlayer(emptyTile)
        val truck = createObject("coal_truck", emptyTile.addY(1))
        player.equipment.set(EquipSlot.Hat.index, "seers_headband_3")
        player["coal_truck_coal_count"] = 195
        player.inventory.add("coal")

        player.itemOnObject(truck, player.inventory.indexOf("coal"))
        tick()

        assertEquals(196, player["coal_truck_coal_count", 0])
        assertEquals(196, player["coal_truck_coal", 0])
        assertEquals(0, player.inventory.count("coal"))
        assertTrue(player.containsMessage("The coal truck is now full."))
    }
}
