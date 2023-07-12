package world.gregs.voidps.engine.contain.transact

import world.gregs.voidps.engine.contain.Inventory
import world.gregs.voidps.engine.entity.item.Item

/**
 * Allows for saving and reverting the state of an inventory
 */
class StateManager(
    private val inventory: Inventory
) {

    private var history: Array<Item>? = null

    /**
     * Checks if StateManager has a previously saved state the inventory.
     * @return whether the StateManager instance has saved the current state of the inventory
     */
    fun hasSaved(): Boolean = history != null

    /**
     * Saves the current state of items in the inventory.
     */
    fun save() {
        if (history == null) {
            history = inventory.items.copyOf()
        }
    }

    /**
     * Removes the saved state.
     */
    fun clear() {
        history = null
    }

    /**
     * Undoes any changes made to the inventory since the last saved state and removes the saved state.
     * @return a boolean indicating whether the revert was successful
     */
    fun revert(): Boolean {
        inventory.data = history ?: return false
        clear()
        return true
    }

}