package world.gregs.voidps.world.interact.entity.player.effect.degrade

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.inv.swap
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.script.WorldTest

class DegradeTest : WorldTest() {

    @Test
    fun `Degrade item with player variable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        val inventoryId = player.equipment.id
        player.equipment.set(slot, "binding_necklace")
        assertEquals(16, Degrade.charges(player, inventoryId, slot))
        Degrade.discharge(player, inventoryId, slot, amount = 6)
        assertEquals(10, Degrade.charges(player, inventoryId, slot))
        assertFalse(player.equipment[slot].isEmpty())

        Degrade.clear(player, inventoryId, slot)
        assertTrue(player.equipment[slot].isEmpty())
        assertEquals(0, Degrade.charges(player, inventoryId, slot))
    }

    @Test
    fun `Do nothing on item degrade`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        val inventoryId = player.equipment.id
        player.equipment.set(slot, "camulet")
        assertEquals(4, Degrade.charges(player, inventoryId, slot))
        Degrade.discharge(player, inventoryId, slot, amount = 3)
        assertEquals(1, Degrade.charges(player, inventoryId, slot))
        assertFalse(player.equipment[slot].isEmpty())

        Degrade.clear(player, inventoryId, slot)
        assertEquals("camulet", player.equipment[slot].id)
        assertEquals(0, Degrade.charges(player, inventoryId, slot))
    }

    @Test
    fun `Degrade item without variable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        player.equipment.set(slot, "black_mask_8")
        val inventoryId = player.equipment.id
        assertEquals(1, Degrade.charges(player, inventoryId, slot))
        Degrade.discharge(player, inventoryId, slot)
        assertEquals(1, Degrade.charges(player, inventoryId, slot))
        assertEquals("black_mask_7", player.equipment[slot].id)
    }

    @Test
    fun `Can't discharge item without charges`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        player.equipment.set(slot, "black_mask")
        val inventoryId = player.equipment.id
        assertFalse(Degrade.discharge(player, inventoryId, -1)) // Invalid
        assertFalse(Degrade.discharge(player, inventoryId, 0)) // Empty
        assertFalse(Degrade.discharge(player, inventoryId, slot)) // No charges
    }

    @Test
    fun `Degrade per item`() {
        val player = createPlayer("player")
        val equipSlot = EquipSlot.Weapon.index
        player.equipment.set(equipSlot, "chaotic_rapier")
        val equipmentId = player.equipment.id
        val inventoryId = player.inventory.id
        assertEquals(30000, Degrade.charges(player, equipmentId, equipSlot))
        assertEquals(0, Degrade.charges(player, inventoryId, 0))
        Degrade.discharge(player, equipmentId, equipSlot)
        assertEquals(29999, Degrade.charges(player, equipmentId, equipSlot))
        assertEquals("chaotic_rapier", player.equipment[equipSlot].id)

        player.equipment.move(equipSlot, player.inventory)

        assertTrue(player.equipment[equipSlot].isEmpty())
        assertEquals("chaotic_rapier", player.inventory[0].id)
        assertEquals(29999, Degrade.charges(player, inventoryId, 0))
    }

    @Test
    fun `Switch two identical degraded items`() {
        val player = createPlayer("player")
        val equipSlot = EquipSlot.Weapon.index
        val equipmentId = player.equipment.id
        val inventorySlot = 0
        val inventoryId = player.inventory.id
        player.equipment.set(equipSlot, "chaotic_rapier")
        player.inventory.set(inventorySlot, "chaotic_rapier")
        assertEquals(30000, Degrade.charges(player, equipmentId, equipSlot))
        assertEquals(30000, Degrade.charges(player, inventoryId, inventorySlot))

        Degrade.discharge(player, equipmentId, equipSlot)
        Degrade.discharge(player, inventoryId, inventorySlot, amount = 100)
        assertEquals(29999, Degrade.charges(player, equipmentId, equipSlot))
        assertEquals(29900, Degrade.charges(player, inventoryId, inventorySlot))

        player.equipment.swap(equipSlot, player.inventory, inventorySlot)

        assertEquals(29900, Degrade.charges(player, equipmentId, equipSlot))
        assertEquals(29999, Degrade.charges(player, inventoryId, inventorySlot))
    }
}