package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container

/**
 * Transaction operation for moving an item inside a container.
 * The moveToLimit operation moves items from the current container to another container until
 * the target container reaches its capacity or the desired amount is moved.
 */
interface MoveItemLimit : RemoveItemLimit {

    /**
     * Moves items from the current container to another container until the target
     * container reaches its capacity or the desired amount is moved.
     * @param id the identifier of the item to be moved.
     * @param amount the number of items to be moved.
     * @param target the target container for the items.
     * @return the number of items actually moved.
     */
    fun moveToLimit(id: String, amount: Int, target: Container, replace: String = id): Int {
        if (failed) {
            return 0
        }

        val transaction = link(target)
        val added = transaction.addToLimit(replace, amount)
        if (added == 0) {
            return 0
        }
        val removed = removeToLimit(id, added)
        if (failed) {
            return 0
        }
        if (removed < added) {
            // Undo and redo changes to target so items are in the correct place
            transaction.remove(replace, added)
            transaction.changes.clear()
            if (removed > 0) {
                transaction.add(replace, removed)
            }
        }
        return removed
    }

    /**
     * Moves as many items from the current container to another container until the target
     * container reaches its capacity or everything is moved.
     * @param target the target container for the items.
     */
    fun moveAllToLimit(target: Container) {
        if (failed) {
            return
        }
        for (index in container.indices) {
            val item = container[index]
            if (item.isEmpty()) {
                continue
            }
            moveToLimit(item.id, item.amount, target)
        }
    }

}