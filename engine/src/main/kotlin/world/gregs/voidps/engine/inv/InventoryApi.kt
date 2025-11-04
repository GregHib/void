package world.gregs.voidps.engine.inv

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

interface InventoryApi {

    /**
     * An item slot change in an inventory.
     * Note: emitted on every individual change
     * For a general "any change" occurred notification use [inventoryUpdated]
     */
    fun slotChanged(inventory: String = "*", slot: Int? = null, handler: Player.(change: InventorySlotChanged) -> Unit) {
        slots.getOrPut("$inventory:${slot ?: "*"}") { mutableListOf() }.add(handler)
    }

    fun slotChanged(inventory: String = "*", slot: EquipSlot, handler: Player.(change: InventorySlotChanged) -> Unit) {
        slotChanged(inventory, slot.index, handler)
    }

    fun inventoryUpdated(inventory: String = "*", handler: Player.(inventory: String, changed: List<InventorySlotChanged>) -> Unit) {
        updates.getOrPut(inventory) { mutableListOf() }.add(handler)
    }

    /**
     * An item slot updated to add an item to an inventory.
     */
    fun itemAdded(item: String = "*", inventory: String, slot: Int? = null, handler: Player.(ItemAdded) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { id ->
            added.getOrPut("$id:$inventory:${slot ?: "*"}") { mutableListOf() }.add(handler)
        }
    }

    fun itemAdded(item: String = "*", inventory: String, slot: EquipSlot, handler: Player.(ItemAdded) -> Unit) {
        itemAdded(item, inventory, slot.index, handler)
    }

    /**
     * An item slot updated to remove an item from an inventory.
     */
    fun itemRemoved(item: String = "*", inventory: String, slot: Int? = null, handler: Player.(ItemRemoved) -> Unit) {
        Wildcards.find(item, Wildcard.Item) { id ->
            removed.getOrPut("$id:$inventory:${slot ?: "*"}") { mutableListOf() }.add(handler)
        }
    }

    fun itemRemoved(item: String = "*", inventory: String, slot: EquipSlot, handler: Player.(ItemRemoved) -> Unit) {
        itemRemoved(item, inventory, slot.index, handler)
    }

    companion object : AutoCloseable {
        private val updates = Object2ObjectOpenHashMap<String, MutableList<(Player, String, List<InventorySlotChanged>) -> Unit>>(15)
        private val slots = Object2ObjectOpenHashMap<String, MutableList<(Player, InventorySlotChanged) -> Unit>>(20)
        private val added = Object2ObjectOpenHashMap<String, MutableList<(Player, ItemAdded) -> Unit>>(50)
        private val removed = Object2ObjectOpenHashMap<String, MutableList<(Player, ItemRemoved) -> Unit>>(20)

        fun add(player: Player, update: ItemAdded) {
            val (item, inventory, slot) = update
            for (handler in added["${item.id}:$inventory:$slot"] ?: emptyList()) {
                handler(player, update)
            }
            for (handler in added["*:$inventory:$slot"] ?: emptyList()) {
                handler(player, update)
            }
            for (handler in added["${item.id}:$inventory:*"] ?: emptyList()) {
                handler(player, update)
            }
            for (handler in added["*:$inventory:*"] ?: emptyList()) {
                handler(player, update)
            }
        }

        fun remove(player: Player, update: ItemRemoved) {
            val (inventory, slot, item) = update
            for (handler in removed["${item.id}:$inventory:$slot"] ?: emptyList()) {
                handler(player, update)
            }
            for (handler in removed["*:$inventory:$slot"] ?: emptyList()) {
                handler(player, update)
            }
            for (handler in removed["${item.id}:$inventory:*"] ?: emptyList()) {
                handler(player, update)
            }
            for (handler in removed["*:$inventory:*"] ?: emptyList()) {
                handler(player, update)
            }
        }

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
            added.clear()
            removed.clear()
        }
    }
}