package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container

/**
 * Transaction operation for moving an item inside a container.
 * The moveToLimit operation moves items from the current container to another container until
 * the target container reaches its capacity or the desired quantity is moved.
 */
interface MoveItemLimit : RemoveItem {

    /**
     * Moves items from the current container to another container until the target
     * container reaches its capacity or the desired quantity is moved.
     * @param id the identifier of the item to be moved.
     * @param quantity the number of items to be moved.
     * @param container the target container for the items.
     * @return the number of items actually moved.
     */
    fun moveToLimit(id: String, quantity: Int, container: Container): Int {
        if (failed) {
            return 0
        }
        mark(container)
        remove(id, quantity)
        if (failed) {
            return 0
        }
        return container.txn {
            val added = addToLimit(id, quantity)
            added
        } ?: 0
    }

}