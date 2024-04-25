package world.gregs.voidps.world.interact.entity.player.effect.degrade

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.script.WorldTest

class DegradeTest : WorldTest() {

    @Test
    fun `Degrade item with player variable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        val inventory = player.equipment
        player["binding_necklace_charges"] = 16
        player.equipment.set(slot, "binding_necklace")
        assertEquals(16, inventory.charges(player, slot))
        assertTrue(inventory.discharge(player, slot, amount = 6))
        assertEquals(10, inventory.charges(player, slot))
        assertFalse(player.equipment[slot].isEmpty())

        inventory.discharge(player, slot, 10)
        tick()
        assertTrue(player.equipment[slot].isEmpty())
        assertEquals(0, inventory.charges(player, slot))
    }

    @Test
    fun `Do nothing on item degrade`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        val inventory = player.equipment
        player["camulet_charges"] = 4
        inventory.set(slot, "camulet")
        assertEquals(4, inventory.charges(player, slot))
        assertTrue(inventory.discharge(player, slot, amount = 3))
        assertEquals(1, inventory.charges(player, slot))
        assertFalse(inventory[slot].isEmpty())

        inventory.clearCharges(player, slot)
        assertEquals("camulet", inventory[slot].id)
        assertEquals(0, inventory.charges(player, slot))
    }

    @Test
    fun `Can't degrade item with different charge start`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Weapon.index
        val inventory = player.equipment
        assertFalse(inventory.discharge(player, slot))
        player.equipment.set(slot, "nature_staff")
        player["nature_staff_charges"] = 0
        assertEquals(0, inventory.charges(player, slot))
        assertFalse(inventory.discharge(player, slot))
        assertEquals(0, inventory.charges(player, slot))
        assertEquals("nature_staff", player.equipment[slot].id)
    }

    @Test
    fun `Degrade item without variable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Hat.index
        player.equipment.set(slot, "black_mask_8")
        val inventory = player.equipment
        assertEquals(1, inventory.charges(player, slot))
        assertTrue(inventory.discharge(player, slot))
        tick()
        assertEquals(1, inventory.charges(player, slot))
        assertEquals("black_mask_7", player.equipment[slot].id)
    }

    @Test
    fun `Degrade item without variable into non degradable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        player.equipment.set(slot, "amulet_of_glory_1")
        val inventory = player.equipment
        assertEquals(1, inventory.charges(player, slot))
        assertTrue(inventory.discharge(player, slot))
        tick()
        assertEquals(0, inventory.charges(player, slot))
        assertEquals("amulet_of_glory", player.equipment[slot].id)
    }

    @Test
    fun `Can't discharge item without charges`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Hat.index
        player.equipment.set(slot, "black_mask")
        val inventory = player.equipment
        assertFalse(inventory.discharge(player, -1)) // Invalid
        assertFalse(inventory.discharge(player, 0)) // Empty
        assertFalse(inventory.discharge(player, slot)) // No charges
    }

    @Test
    fun `Degrade per item`() {
        val player = createPlayer("player")
        val equipSlot = EquipSlot.Weapon.index
        player.equipment.set(equipSlot, "chaotic_rapier", 30000)
        val equipment = player.equipment
        val inventory = player.inventory
        assertEquals(30000, equipment.charges(player, equipSlot))
        assertEquals(0, inventory.charges(player, 0))
        assertTrue(equipment.discharge(player, equipSlot))
        assertEquals(29999, equipment.charges(player, equipSlot))
        assertEquals("chaotic_rapier", player.equipment[equipSlot].id)

        player.equipment.move(equipSlot, player.inventory)

        assertTrue(player.equipment[equipSlot].isEmpty())
        assertEquals("chaotic_rapier", player.inventory[0].id)
        assertEquals(29999, inventory.charges(player, 0))
    }

    @Test
    fun `Switch two identical degraded items`() {
        val player = createPlayer("player")
        val equipSlot = EquipSlot.Weapon.index
        val equipment = player.equipment
        val inventorySlot = 0
        val inventory = player.inventory
        player.equipment.set(equipSlot, "chaotic_rapier", 30000)
        player.inventory.set(inventorySlot, "chaotic_rapier", 30000)
        assertEquals(30000, equipment.charges(player, equipSlot))
        assertEquals(30000, inventory.charges(player, inventorySlot))

        assertTrue(equipment.discharge(player, equipSlot))
        assertTrue(inventory.discharge(player, inventorySlot, amount = 100))
        assertEquals(29999, equipment.charges(player, equipSlot))
        assertEquals(29900, inventory.charges(player, inventorySlot))

        player.equipment.swap(equipSlot, player.inventory, inventorySlot)

        assertEquals(29900, equipment.charges(player, equipSlot))
        assertEquals(29999, inventory.charges(player, inventorySlot))
    }

    @Test
    fun `Charge item with player variable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        val inventory = player.equipment
        player["binding_necklace_charges"] = 16
        player.equipment.set(slot, "binding_necklace")
        assertEquals(16, inventory.charges(player, slot))
        assertTrue(inventory.discharge(player, slot, amount = 6))
        assertEquals(10, inventory.charges(player, slot))
        assertFalse(player.equipment[slot].isEmpty())

        assertTrue(inventory.charge(player, slot, amount = 5))
        assertFalse(player.equipment[slot].isEmpty())
        assertEquals(15, inventory.charges(player, slot))
    }

    @Test
    fun `Can't charge item charge`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Hat.index
        val inventory = player.equipment
        player.equipment.set(slot, "black_mask_6")
        assertFalse(inventory.charge(player, EquipSlot.Shield.index))
        assertEquals(1, inventory.charges(player, slot))
        assertTrue(inventory.charge(player, slot))
        assertEquals(1, inventory.charges(player, slot))
    }

    @Test
    fun `Can't charge non-chargeable item`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Weapon.index
        val inventory = player.equipment
        player.equipment.set(slot, "abyssal_whip")
        assertEquals(0, inventory.charges(player, slot))
        assertFalse(inventory.charge(player, slot))
        assertEquals(0, inventory.charges(player, slot))
    }

    @Test
    fun `Can't do anything with invalid slot`() {
        val player = createPlayer("player")
        val slot = EquipSlot.None.index
        val inventory = player.equipment
        assertEquals(0, inventory.charges(player, slot))
        assertFalse(inventory.discharge(player, slot))
        assertFalse(inventory.clearCharges(player, slot))
        assertFalse(inventory.charge(player, slot))
    }
}