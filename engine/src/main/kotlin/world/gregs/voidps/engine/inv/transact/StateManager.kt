package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory

/**
 * Allows for saving and reverting the state of an inventory
 */
class StateManager(
    private val inventory: Inventory,
) {

    private var history: Array<Item>? = null
    private var reverts = mutableListOf<() -> Unit>()

    /**
     * Adds a block of code to be executed when the inventory state is reverted.
     */
    fun onRevert(block: () -> Unit) {
        reverts.add(block)
    }

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
        reverts.clear()
    }

    /**
     * Undoes any changes made to the inventory since the last saved state and removes the saved state.
     * @return a boolean indicating whether the revert was successful
     */
    fun revert(): Boolean {
        inventory.data = history ?: return false
        for (block in reverts) {
            block.invoke()
        }
        reverts.clear()
        clear()
        return true
    }
}
