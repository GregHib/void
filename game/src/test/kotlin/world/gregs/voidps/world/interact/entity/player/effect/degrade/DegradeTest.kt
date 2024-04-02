package world.gregs.voidps.world.interact.entity.player.effect.degrade

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.engine.inv.swap
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.world.script.WorldTest

class DegradeTest : WorldTest() {

    @Test
    fun `Degrade item with player variable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        val inventory = player.equipment
        player.equipment.set(slot, "binding_necklace")
        assertEquals(16, Degrade.charges(player, inventory, slot))
        Degrade.discharge(player, inventory, slot, amount = 6)
        assertEquals(10, Degrade.charges(player, inventory, slot))
        assertFalse(player.equipment[slot].isEmpty())

        Degrade.clear(player, inventory, slot)
        assertTrue(player.equipment[slot].isEmpty())
        assertEquals(0, Degrade.charges(player, inventory, slot))
    }

    @Test
    fun `Do nothing on item degrade`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        val inventory = player.equipment
        player.equipment.set(slot, "camulet")
        assertEquals(4, Degrade.charges(player, inventory, slot))
        Degrade.discharge(player, inventory, slot, amount = 3)
        assertEquals(1, Degrade.charges(player, inventory, slot))
        assertFalse(player.equipment[slot].isEmpty())

        Degrade.clear(player, inventory, slot)
        assertEquals("camulet", player.equipment[slot].id)
        assertEquals(0, Degrade.charges(player, inventory, slot))
    }

    @Test
    fun `Degrade item without variable`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        player.equipment.set(slot, "black_mask_8")
        val inventory = player.equipment
        assertEquals(1, Degrade.charges(player, inventory, slot))
        Degrade.discharge(player, inventory, slot)
        assertEquals(1, Degrade.charges(player, inventory, slot))
        assertEquals("black_mask_7", player.equipment[slot].id)
    }

    @Test
    fun `Can't discharge item without charges`() {
        val player = createPlayer("player")
        val slot = EquipSlot.Amulet.index
        player.equipment.set(slot, "black_mask")
        val inventory = player.equipment
        assertFalse(Degrade.discharge(player, inventory, -1)) // Invalid
        assertFalse(Degrade.discharge(player, inventory, 0)) // Empty
        assertFalse(Degrade.discharge(player, inventory, slot)) // No charges
    }

    @Test
    fun `Degrade per item`() {
        val player = createPlayer("player")
        val equipSlot = EquipSlot.Weapon.index
        player.equipment.set(equipSlot, "chaotic_rapier")
        val equipment = player.equipment
        val inventory = player.inventory
        assertEquals(30000, Degrade.charges(player, equipment, equipSlot))
        assertEquals(0, Degrade.charges(player, inventory, 0))
        Degrade.discharge(player, equipment, equipSlot)
        assertEquals(29999, Degrade.charges(player, equipment, equipSlot))
        assertEquals("chaotic_rapier", player.equipment[equipSlot].id)

        player.equipment.move(equipSlot, player.inventory)

        assertTrue(player.equipment[equipSlot].isEmpty())
        assertEquals("chaotic_rapier", player.inventory[0].id)
        assertEquals(29999, Degrade.charges(player, inventory, 0))
    }

    @Test
    fun `Switch two identical degraded items`() {
        val player = createPlayer("player")
        val equipSlot = EquipSlot.Weapon.index
        val equipment = player.equipment
        val inventorySlot = 0
        val inventory = player.inventory
        player.equipment.set(equipSlot, "chaotic_rapier")
        player.inventory.set(inventorySlot, "chaotic_rapier")
        assertEquals(30000, Degrade.charges(player, equipment, equipSlot))
        assertEquals(30000, Degrade.charges(player, inventory, inventorySlot))

        Degrade.discharge(player, equipment, equipSlot)
        Degrade.discharge(player, inventory, inventorySlot, amount = 100)
        assertEquals(29999, Degrade.charges(player, equipment, equipSlot))
        assertEquals(29900, Degrade.charges(player, inventory, inventorySlot))

        player.equipment.swap(equipSlot, player.inventory, inventorySlot)

        assertEquals(29900, Degrade.charges(player, equipment, equipSlot))
        assertEquals(29999, Degrade.charges(player, inventory, inventorySlot))
    }
}