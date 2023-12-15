package world.gregs.voidps.world.interact.entity.player.effect.degrade

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace

/**
 * Tracks and manages charges associated with players' items
 *
 * Item charges are tracked in three ways:
 * 1. Item level
 *  Charge is reflected in the items id and name e.g. black_mask_8
 *  Reducing a charge replaces the item
 * 2. Player level
 *  Charge is stored as a player variable e.g. ring_of_recoil
 *  Reducing a charge has no effect on the item
 * 3. Inventory level
 *  Charge is stored per individual item in an inventories slot
 *  Reducing a charge has no effect on the item
 *
 * An item charge being reduced to zero can:
 * 1. Destroy (remove) the item
 * 2. Replaced the item e.g. chaotic_rapier_broken
 * 3. Do nothing
 */
object Degrade {

    /**
     * Key used to track item charges at an [inventory] level
     */
    fun variable(inventory: String, slot: Int) = "charges_${inventory}_$slot"

    /**
     * Check [player]'s charges for the item at [slot] in [inventory]
     */
    fun charges(player: Player, inventory: String, slot: Int): Int {
        val item = player.inventories.inventory(inventory).getOrNull(slot) ?: return 0
        // Inventory tracked item
        val charge: Int? = player.getOrNull(variable(inventory, slot))
        if (charge != null) {
            return charge
        }

        // Player variable
        val variable = item.def["charge", "${item.id}_charges"]
        if (player.contains(variable)) {
            return player[variable]
        }

        // Return default
        return item.def["charges", 0]
    }

    /**
     * Reduce the item at [slot] in [inventory]'s charges by [amount] and [degrade] if charge <= 0
     */
    fun discharge(player: Player, inventory: String, slot: Int, amount: Int = 1) {
        val inv = player.inventories.inventory(inventory)
        val item = inv.getOrNull(slot) ?: return

        // Determine tracking variable to use
        val playerCharge = item.def["player_charge", false]
        val variable = if (playerCharge) item.def["charge", "${item.id}_charges"] else variable(inventory, slot)
        val charge = player.getOrNull(variable) ?: item.def["charges", 0]

        // Calculated reduced charge
        val reduced = charge - amount
        if (reduced > 0) {
            player[variable] = reduced
            return
        }

        // Clear charges and degrade item
        player.clear(variable)
        degrade(inv, item, slot)
    }

    /**
     * Removes all charges for item at [slot] in [inventory]
     */
    fun degrade(player: Player, inventory: String, slot: Int) {
        val inv = player.inventories.inventory(inventory)
        val item = inv.getOrNull(slot) ?: return

        // Clear charges and degrade item
        player.clear(variable(inventory, slot))
        player.clear(item.def["charge", "${item.id}_charges"])
        degrade(inv, item, slot)
    }

    /**
     * Replace or remove the item after it's charges have been depleted
     */
    private fun degrade(inventory: Inventory, item: Item, slot: Int) {
        val replacement: String? = item.def.getOrNull("degrade")
        if (replacement != null) {
            inventory.replace(slot, item.id, replacement)
            return
        }

        inventory.remove(slot, item.id, item.amount)
        // TODO message
    }
}