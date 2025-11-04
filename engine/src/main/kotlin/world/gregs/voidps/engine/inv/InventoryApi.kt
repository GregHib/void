package world.gregs.voidps.engine.inv

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

interface InventoryApi {

    /**
     * An item slot change in an inventory.
     * Note: emitted on every individual change
     * For a general "any change" occurred notification use [inventoryUpdated]
     */
    fun slotChanged(inventory: String = "*", slot: Int? = null, handler: Player.(InventorySlotChanged) -> Unit) {
        slots.getOrPut("$inventory:${slot ?: "*"}") { mutableListOf() }.add(handler)
    }

    fun slotChanged(inventory: String = "*", slot: EquipSlot, handler: Player.(InventorySlotChanged) -> Unit) {
        slotChanged(inventory, slot.index, handler)
    }

    fun inventoryUpdated(inventory: String = "*", handler: Player.(String, List<InventorySlotChanged>) -> Unit) {
        updates.getOrPut(inventory) { mutableListOf() }.add(handler)
    }

    companion object : AutoCloseable {
        private val updates = Object2ObjectOpenHashMap<String, MutableList<(Player, String, List<InventorySlotChanged>) -> Unit>>(15)
        private val slots = Object2ObjectOpenHashMap<String, MutableList<(Player, InventorySlotChanged) -> Unit>>(15)

        fun changed(player: Player, slot: InventorySlotChanged) {
            for (handler in slots["${slot.inventory}:${slot.index}"] ?: emptyList()) {
                handler(player, slot)
            }
            for (handler in slots["*:${slot.index}"] ?: emptyList()) {
                handler(player, slot)
            }
            for (handler in slots["${slot.inventory}:*"] ?: emptyList()) {
                handler(player, slot)
            }
            for (handler in slots["*:*"] ?: return) {
                handler(player, slot)
            }
        }

        fun update(player: Player, inventory: String, list: List<InventorySlotChanged>) {
            for (handler in updates[inventory] ?: emptyList()) {
                handler(player, inventory, list)
            }
            for (handler in updates["*"] ?: emptyList()) {
                handler(player, inventory, list)
            }
        }

        override fun close() {
            updates.clear()
            slots.clear()
        }
    }
}