package world.gregs.voidps.world.interact.entity.player.effect.degrade

import world.gregs.voidps.engine.client.message
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
     * Reduce the item at [slot] in [inventory]'s charges by [amount] and [clear] if charge <= 0
     */
    fun discharge(player: Player, inventory: String, slot: Int, amount: Int = 1): Boolean {
        val inv = player.inventories.inventory(inventory)
        val item = inv.getOrNull(slot) ?: return false
        if (item.isEmpty()) {
            return false
        }

        val variable = variable(item, inventory, slot)
        val charge = player.getOrNull(variable) ?: item.def["charges", 0]
        if (charge <= 0) {
            return false
        }

        // Calculated reduced charge
        val reduced = charge - amount
        if (reduced > 0) {
            player[variable] = reduced
            return true
        }

        // Clear charges and degrade item
        degrade(player, inv, item, slot, variable)
        return true
    }

    /**
     * Removes all charges for item at [slot] in [inventory]
     */
    fun clear(player: Player, inventory: String, slot: Int) {
        val inv = player.inventories.inventory(inventory)
        val item = inv.getOrNull(slot) ?: return

        val variable = variable(item, inventory, slot)

        // Clear charges and degrade item
        degrade(player, inv, item, slot, variable)
    }

    /**
     * Determine tracking variable to use
     */
    private fun variable(item: Item, inventory: String, slot: Int): String {
        val playerCharge = item.def["player_charge", false]
        return if (playerCharge) item.def["charge", "${item.id}_charges"] else variable(inventory, slot)
    }

    /**
     * Replace or remove the item after it's charges have been depleted
     */
    private fun degrade(player: Player, inventory: Inventory, item: Item, slot: Int, variable: String) {
        val replacement: String? = item.def.getOrNull("degrade")
        if (replacement == null) {
            player[variable] = 0
            return
        }
        player.clear(variable)
        if (replacement == "destroy") {
            inventory.remove(slot, item.id, item.amount)
        } else {
            inventory.replace(slot, item.id, replacement)
        }
        val message: String = item.def.getOrNull("degrade_message") ?: return
        player.message(message)
    }
}