package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

/**
 * Transaction operation for moving an item inside a container.
 * The move operation moves an item from the current container to another container.
 */
interface MoveItem : RemoveItem, AddItem, ClearItem {

    /**
     * Moves all items from one container to another
     * @param target the target container for the items.
     */
    fun moveAll(target: Container) {
        if (failed) {
            return
        }
        for (index in container.items.indices) {
            val item = container.getItem(index)
            if (item.isEmpty()) {
                continue
            }
            move(index, target)
        }
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
        if (!container.inBounds(fromIndex)) {
            error = TransactionError.Invalid
            return
        }
        val fromItem = container.getItem(fromIndex)
        if (fromItem.isEmpty()) {
            error = TransactionError.Deficient()
            return
        }
        val transaction = linkTransaction(target)
        if (!target.stackRule.stackable(fromItem.id) && fromItem.amount == 1) {
            // Move single non-stackable items to keep charges
            val freeIndex = target.freeIndex()
            if (freeIndex == -1) {
                transaction.error = TransactionError.Full()
                return
            }
            transaction.set(freeIndex, fromItem, moved = true)
        } else {
            transaction.add(fromItem.id, fromItem.amount)
        }
        set(fromIndex, item = null, moved = true)
    }

    /**
     * Moves an item from one index to another in the same container
     * @param fromIndex the index of the item in the current container.
     * @param toIndex the index where the item will be placed in the current container.
     */
    fun move(fromIndex: Int, toIndex: Int) {
        move(fromIndex, container, toIndex)
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
        if (!container.inBounds(fromIndex) || !target.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        val fromItem = container.getItem(fromIndex)
        if (fromItem.isEmpty()) {
            error = TransactionError.Deficient()
            return
        }
        val transaction = linkTransaction(target)
        val toItem = target.getItem(toIndex)
        if (toItem.isEmpty()) {
            transaction.set(toIndex, fromItem, moved = true)
        } else if (!mergeStacks(transaction, fromItem.id, fromItem.amount, target, toItem, toIndex)) {
            return
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
        remove(id, quantity)
        if (failed) {
            return
        }
        val transaction = linkTransaction(target)
        transaction.add(id, quantity)
    }

    /**
     * Moves a specific quantity of an item to another index
     * @param id the identifier of the item to be moved.
     * @param quantity the number of items to be moved.
     * @param toIndex the index of the target stack in the current container
     */
    fun move(id: String, quantity: Int, toIndex: Int) {
        move(id, quantity, container, toIndex)
    }

    /**
     * Moves a specific quantity of an item from the current container to an index in another container.
     * @param id the identifier of the item to be moved.
     * @param quantity the number of items to be moved.
     * @param target the target container for the item.
     * @param toIndex the index of the target stack in the [target] container
     */
    fun move(id: String, quantity: Int, target: Container, toIndex: Int) {
        remove(id, quantity)
        if (failed) {
            return
        }
        val transaction = linkTransaction(target)
        val toItem = target.getItem(toIndex)
        if (toItem.isEmpty()) {
            if (target.stackRule.stackable(id)) {
                transaction.set(toIndex, Item(id, quantity), moved = true)
            } else {
                transaction.add(id, quantity)
            }
        } else if (!mergeStacks(transaction, id, quantity, target, toItem, toIndex)) {
            return
        }
    }

    /**
     * Merge two stacks of items in the specified container.
     *
     * @param transaction the current container transaction
     * @param id the ID of the items to be merged
     * @param quantity the number of items to be added to the stack
     * @param target the container in which the stacks are located
     * @param toItem the target stack of items
     * @param toIndex the index of the target stack in the [target] container
     * @return true if the two stack were merged, otherwise false when the items are not stackable
     */
    private fun mergeStacks(transaction: MoveItem, id: String, quantity: Int, target: Container, toItem: Item, toIndex: Int): Boolean {
        if (id != toItem.id || !target.stackRule.stackable(toItem.id)) {
            transaction.error = TransactionError.Full()
            return false
        }
        transaction.increaseStack(toIndex, quantity)
        return true
    }
}