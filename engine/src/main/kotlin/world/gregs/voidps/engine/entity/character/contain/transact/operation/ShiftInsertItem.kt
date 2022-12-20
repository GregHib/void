package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

/**
 * Transaction operation for inserting items in a container.
 * This operation allows inserting an item from one container to another container at
 * a specific index, and shifting the other items to make room for the inserted item.
 */
interface ShiftInsertItem : TransactionOperation {

    /**
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
            error = TransactionError.Invalid
            return
        }
        val item = container.getItem(fromIndex)
        if (item.isEmpty()) {
            error = TransactionError.Invalid
            return
        }
        insert(target, toIndex, item.id, item.amount)
        set(fromIndex, null, moved = true)
    }

    /**
     * Inserts an item to a specific container index, and shifts the other items to make room for the inserted item.
     * @param id the identifier of the item to be inserted.
     * @param amount the number of items to be inserted.
     * @param toIndex the index where the item will be inserted in the target container.
     */
    fun shiftInsert(id: String, amount: Int, toIndex: Int) {
        if (failed) {
            return
        }
        if (!container.stackRule.stackable(id) && amount != 1) {
            error = TransactionError.Invalid
            return
        }
        if (container.itemRule.restricted(id) || !container.removalCheck.exceedsMinimum(amount) || !container.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        insert(container, toIndex, id, amount)
    }

    private fun insert(target: Container, toIndex: Int, id: String, amount: Int) {
        val freeIndex = target.freeIndex()
        if (freeIndex == -1) {
            error = TransactionError.Full()
            return
        }
        val transaction = linkTransaction(target)
        // Shift the items in the target container from the insertion index to the end, one index to the right
        for (index in freeIndex downTo toIndex + 1) {
            transaction.set(index, target.getItem(index - 1))
        }
        // Insert the item at the specified index in the target container
        transaction.set(toIndex, Item(id, amount), moved = true)
    }

}