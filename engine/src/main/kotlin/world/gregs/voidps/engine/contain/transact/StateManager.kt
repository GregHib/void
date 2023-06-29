package world.gregs.voidps.engine.contain.transact

import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.entity.item.Item

/**
 * Allows for saving and reverting the state of a container
 */
class StateManager(
    private val container: Container
) {

    private var history: Array<Item>? = null

    /**
     * Checks if StateManager has a previously saved state the container.
     * @return whether the StateManager instance has saved the current state of the container
     */
    fun hasSaved(): Boolean = history != null

    /**
     * Saves the current state of items in the container.
     */
    fun save() {
        if (history == null) {
            history = container.items.copyOf()
        }
    }

    /**
     * Removes the saved state.
     */
    fun clear() {
        history = null
    }

    /**
     * Undoes any changes made to the container since the last saved state and removes the saved state.
     * @return a boolean indicating whether the revert was successful
     */
    fun revert(): Boolean {
        container.data = history ?: return false
        clear()
        return true
    }

}