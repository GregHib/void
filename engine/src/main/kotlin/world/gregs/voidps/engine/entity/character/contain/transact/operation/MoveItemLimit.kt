package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container

/**
 * Transaction operation for moving an item inside a container.
 * The moveToLimit operation moves items from the current container to another container until
 * the target container reaches its capacity or the desired quantity is moved.
 */
interface MoveItemLimit : RemoveItemLimit {

    /**
     * Moves items from the current container to another container until the target
     * container reaches its capacity or the desired quantity is moved.
     * @param id the identifier of the item to be moved.
     * @param quantity the number of items to be moved.
     * @param target the target container for the items.
     * @return the number of items actually moved.
     */
    fun moveToLimit(id: String, quantity: Int, target: Container): Int {
        if (failed) {
            return 0
        }

        val transaction = linkTransaction(target)
        val added = transaction.addToLimit(id, quantity)
        if (added == 0) {
            return 0
        }
        val removed = removeToLimit(id, added)
        if (failed) {
            return 0
        }
        if (removed < added) {
            transaction.remove(id, added - removed)
        }
        return removed
    }

    /**
     * Moves as many items from the current container to another container until the target
     * container reaches its capacity or the desired quantity is moved.
     * @param target the target container for the items.
     */
    fun moveAllToLimit(target: Container) {
        if (failed) {
            return
        }
        for (index in container.items.indices) {
            val item = container.getItem(index)
            if (item.isEmpty()) {
                continue
            }
            moveToLimit(item.id, item.amount, target)
        }
    }

}