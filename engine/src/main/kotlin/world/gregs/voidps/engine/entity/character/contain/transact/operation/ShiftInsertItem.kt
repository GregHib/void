package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for inserting items in a container.
 * This operation allows inserting an item from one container to another container at
 * a specific index, and shifting the other items to make room for the inserted item.
 */
interface ShiftInsertItem : TransactionOperation {

    /**
     *
     * Inserts an item from one container to another container at a specific
     * index, and shifts the other items to make room for the inserted item.
     * @param fromIndex the index of the item to be inserted in the source container.
     * @param target the target container where the item will be inserted.
     * @param toIndex the index where the item will be inserted in the target container.
     */
    fun shiftInsert(fromIndex: Int, target: Container, toIndex: Int) {
        if (failed) {
            return
        }
        if (!container.inBounds(fromIndex) || !target.inBounds(toIndex)) {
            error(TransactionError.Invalid)
            return
        }
        val item = container.getItem(fromIndex)
        if (item.isEmpty()) {
            error(TransactionError.Invalid)
            return
        }
        val freeIndex = target.freeIndex()
        if (freeIndex == -1) {
            error(TransactionError.Full())
            return
        }
        val transaction = linkTransaction(target)
        // Shift the items in the target container from the insertion index to the end, one index to the right
        for (index in freeIndex downTo toIndex + 1) {
            transaction.set(index, target.getItem(index - 1))
        }
        // Insert the item at the specified index in the target container
        transaction.set(toIndex, item, moved = true)
        set(fromIndex, null, moved = true)
    }

}