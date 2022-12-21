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
        for (index in container.indices) {
            val item = container[index]
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
        val fromItem = container[fromIndex]
        if (fromItem.isEmpty()) {
            error = TransactionError.Deficient()
            return
        }
        val transaction = link(target)
        if (!target.stackable(fromItem.id) && fromItem.amount == 1) {
            // Move single non-stackable items to keep charges
            val freeIndex = target.freeIndex()
            if (freeIndex == -1) {
                transaction.error = TransactionError.Full()
                return
            }
            transaction.set(freeIndex, fromItem, from = container.id)
        } else {
            transaction.add(fromItem.id, fromItem.amount)
        }
        set(fromIndex, item = null, to = target.id)
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
        val fromItem = container[fromIndex]
        if (fromItem.isEmpty()) {
            error = TransactionError.Deficient()
            return
        }
        val transaction = link(target)
        val toItem = target[toIndex]
        if (toItem.isEmpty()) {
            transaction.set(toIndex, fromItem, from = container.id)
        } else if (!mergeStacks(transaction, fromItem.id, fromItem.amount, target, toItem, toIndex)) {
            return
        }
        set(fromIndex, item = null, to = target.id)
    }

    /**
     * Moves a specific amount of an item from the current container to another container.
     * @param id the identifier of the item to be moved.
     * @param amount the number of items to be moved.
     * @param target the target container for the item.
     */
    fun move(id: String, amount: Int, target: Container) {
        remove(id, amount)
        if (failed) {
            return
        }
        val transaction = link(target)
        transaction.add(id, amount)
    }

    /**
     * Moves a specific amount of an item to another index
     * @param id the identifier of the item to be moved.
     * @param amount the number of items to be moved.
     * @param toIndex the index of the target stack in the current container
     */
    fun move(id: String, amount: Int, toIndex: Int) {
        move(id, amount, container, toIndex)
    }

    /**
     * Moves a specific amount of an item from the current container to an index in another container.
     * @param id the identifier of the item to be moved.
     * @param amount the number of items to be moved.
     * @param target the target container for the item.
     * @param toIndex the index of the target stack in the [target] container
     */
    fun move(id: String, amount: Int, target: Container, toIndex: Int) {
        remove(id, amount)
        if (failed) {
            return
        }
        val transaction = link(target)
        val toItem = target[toIndex]
        if (toItem.isEmpty()) {
            if (target.stackable(id)) {
                transaction.set(toIndex, Item(id, amount), from = container.id)
            } else {
                transaction.add(id, amount)
            }
        } else if (!mergeStacks(transaction, id, amount, target, toItem, toIndex)) {
            return
        }
    }

    /**
     * Merge two stacks of items in the specified container.
     *
     * @param transaction the current container transaction
     * @param id the ID of the items to be merged
     * @param amount the number of items to be added to the stack
     * @param target the container in which the stacks are located
     * @param toItem the target stack of items
     * @param toIndex the index of the target stack in the [target] container
     * @return true if the two stack were merged, otherwise false when the items are not stackable
     */
    private fun mergeStacks(transaction: MoveItem, id: String, amount: Int, target: Container, toItem: Item, toIndex: Int): Boolean {
        if (id != toItem.id || !target.stackable(toItem.id)) {
            transaction.error = TransactionError.Full()
            return false
        }
        transaction.increaseStack(toIndex, amount)
        return true
    }
}