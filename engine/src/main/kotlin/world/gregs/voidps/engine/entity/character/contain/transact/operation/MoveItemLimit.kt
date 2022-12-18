package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

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
     * @param container the target container for the items.
     * @return the number of items actually moved.
     */
    fun moveToLimit(id: String, quantity: Int, container: Container): Int {
        if (failed) {
            return 0
        }
        mark(container)

        val added = addToLimit(container, id, quantity)
        if (added == 0) {
            return 0
        }
        val removed = removeToLimit(id, quantity)
        if (failed) {
            return 0
        }
        if (removed < added) {
            container.txn { remove(id, added - removed) }
        }
        return removed
    }

    private fun addToLimit(container: Container, id: String, quantity: Int): Int {
        var added = 0
        val transaction = container.transaction {
            added = addToLimit(id, quantity)
        }
        if (!transaction.commit()) {
            val error = transaction.error
            if (error is TransactionError.Full) {
                return 0
            }
            error(error!!)
            return 0
        }
        return added
    }

}