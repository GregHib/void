package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItem.increaseStack
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * Transaction operation for moving an item inside an inventory.
 * The move operation moves an item from the current inventory to another inventory.
 */
object MoveItem {

    /**
     * Moves all items from one inventory to another
     * @param target the target inventory for the items.
     */
    fun TransactionOperation.moveAll(target: Inventory) {
        if (failed) {
            return
        }
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isEmpty()) {
                continue
            }
            move(index, target)
        }
    }

    /**
     * Moves an item from the current inventory to another inventory, placing it at the first available index.
     * @param fromIndex the index of the item in the current inventory.
     * @param target the target inventory for the item.
     */
    fun TransactionOperation.move(fromIndex: Int, target: Inventory) {
        if (failed) {
            return
        }
        if (!inventory.inBounds(fromIndex)) {
            error = TransactionError.Invalid
            return
        }
        val fromItem = inventory[fromIndex]
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
            transaction.set(freeIndex, fromItem, from = inventory.id, fromIndex = fromIndex)
        } else {
            transaction.add(fromItem.id, fromItem.amount)
        }
        set(fromIndex, item = null)
    }

    /**
     * Moves an item from one index to another in the same inventory
     * @param fromIndex the index of the item in the current inventory.
     * @param toIndex the index where the item will be placed in the current inventory.
     */
    fun TransactionOperation.move(fromIndex: Int, toIndex: Int) {
        move(fromIndex, inventory, toIndex)
    }

    /**
     * Moves an item from the current inventory to another inventory, placing it at a specific index.
     * @param fromIndex the index of the item in the current inventory.
     * @param target the target inventory for the item.
     * @param toIndex the index in the target inventory where the item will be placed.
     */
    fun TransactionOperation.move(fromIndex: Int, target: Inventory, toIndex: Int) {
        if (failed) {
            return
        }
        if (!inventory.inBounds(fromIndex) || !target.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        val fromItem = inventory[fromIndex]
        if (fromItem.isEmpty()) {
            error = TransactionError.Deficient()
            return
        }
        val transaction = link(target)
        val toItem = target[toIndex]
        if (toItem.isEmpty()) {
            transaction.set(toIndex, fromItem, from = inventory.id, fromIndex = fromIndex)
        } else if (!mergeStacks(transaction, fromItem.id, fromItem.amount, target, toItem, toIndex)) {
            return
        }
        set(fromIndex, item = null, from = target.id, fromIndex = toIndex)
    }

    /**
     * Moves a specific amount of an item from the current inventory to another inventory.
     * @param id the identifier of the item to be moved.
     * @param amount the number of items to be moved.
     * @param target the target inventory for the item.
     */
    fun TransactionOperation.move(id: String, amount: Int, target: Inventory) {
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
     * @param toIndex the index of the target stack in the current inventory
     */
    fun TransactionOperation.move(id: String, amount: Int, toIndex: Int) {
        move(id, amount, inventory, toIndex)
    }

    /**
     * Moves a specific amount of an item from the current inventory to an index in another inventory.
     * @param id the identifier of the item to be moved.
     * @param amount the number of items to be moved.
     * @param target the target inventory for the item.
     * @param toIndex the index of the target stack in the [target] inventory
     */
    fun TransactionOperation.move(id: String, amount: Int, target: Inventory, toIndex: Int) {
        remove(id, amount)
        if (failed) {
            return
        }
        val transaction = link(target)
        val toItem = target[toIndex]
        if (toItem.isEmpty()) {
            if (target.stackable(id)) {
                transaction.set(toIndex, Item(id, amount))
            } else {
                transaction.add(id, amount)
            }
        } else if (!mergeStacks(transaction, id, amount, target, toItem, toIndex)) {
            return
        }
    }

    /**
     * Merge two stacks of items in the specified inventory.
     *
     * @param transaction the current inventory transaction
     * @param id the ID of the items to be merged
     * @param amount the number of items to be added to the stack
     * @param target the inventory in which the stacks are located
     * @param toItem the target stack of items
     * @param toIndex the index of the target stack in the [target] inventory
     * @return true if the two stack were merged, otherwise false when the items are not stackable
     */
    private fun TransactionOperation.mergeStacks(transaction: TransactionOperation, id: String, amount: Int, target: Inventory, toItem: Item, toIndex: Int): Boolean {
        if (id != toItem.id || !target.stackable(toItem.id)) {
            transaction.error = TransactionError.Full()
            return false
        }
        transaction.increaseStack(toIndex, amount)
        return true
    }
}
