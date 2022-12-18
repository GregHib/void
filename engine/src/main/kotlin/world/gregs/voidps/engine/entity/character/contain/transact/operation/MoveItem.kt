package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * TODO add move all
 *  move = swap and remove?
 * Transaction operation for moving an item inside a container.
 * The move operation moves an item from the current container to another container.
 */
interface MoveItem : RemoveItem {

    fun move(fromIndex: Int, toIndex: Int) {
        if (failed) {
            return
        }
        if (invalid(fromIndex) || !container.inBounds(toIndex)) {
            error(TransactionError.Invalid)
            return
        }
        val fromItem = container.getItem(fromIndex)
        val toItem = container.getItem(toIndex)
        if (fromItem.isEmpty() || toItem.isNotEmpty()) {
            error(TransactionError.Full(0))
            return
        }
        set(toIndex, fromItem, moved = true)
        set(fromIndex, item = null, moved = true)
    }
    /**
     * Moves an item from the current container to another container, placing it at the first available index.
     * @param fromIndex the index of the item in the current container.
     * @param target the target container for the item.
     */
    fun move(fromIndex: Int, target: Container) {
        if (failed) {
            return
        }
        if (invalid(fromIndex)) {
            error(TransactionError.Invalid)
            return
        }
        val freeIndex = target.freeIndex()
        if (freeIndex == -1) {
            error(TransactionError.TargetFull)
            return
        }
        val transaction = linkTransaction(target)
        transaction.set(freeIndex, container.getItem(fromIndex), moved = true)
        set(fromIndex, item = null, moved = true)
    }

    /**
     * Moves an item from the current container to another container, placing it at a specific index.
     * @param fromIndex the index of the item in the current container.
     * @param target the target container for the item.
     * @param toIndex the index in the target container where the item will be placed.
     */
    fun move(fromIndex: Int, target: Container, toIndex: Int) {
        if (failed) {
            return
        }
        if (invalid(fromIndex) || !target.inBounds(toIndex)) {
            error(TransactionError.Invalid)
            return
        }
        val fromItem = container.getItem(fromIndex)
        val toItem = target.getItem(toIndex)
        if (toItem.isNotEmpty() && (fromItem.id != toItem.id || !target.stackRule.stack(toItem.id))) {
            error(TransactionError.Full(0))
            return
        }
        val transaction = linkTransaction(target)
        if (toItem.isNotEmpty() && fromItem.id == toItem.id) {
            transaction.add(fromItem.id, fromItem.amount)
        } else {
            transaction.set(toIndex, fromItem, moved = true)
        }
        set(fromIndex, item = null, moved = true)
    }

    /**
     * Moves a specific quantity of an item from the current container to another container.
     * @param id the identifier of the item to be moved.
     * @param quantity the number of items to be moved.
     * @param target the target container for the item.
     */
    fun move(id: String, quantity: Int, target: Container) {
        if (failed) {
            return
        }
        if (invalid(id, quantity)) {
            error(TransactionError.Invalid)
            return
        }
        remove(id, quantity)
        if (failed) {
            return
        }
        val transaction = linkTransaction(target)
        transaction.add(id, quantity)
    }

}