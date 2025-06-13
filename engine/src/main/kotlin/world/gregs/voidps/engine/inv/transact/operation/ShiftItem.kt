package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for shifting items within an inventory.
 * This operation allows shifting an item from one index to another within the same inventory,
 * and shifting the other items to make room for the moved item.
 */
object ShiftItem {

    /**
     * Shifts an item from a specified index to the next free index of the inventory,
     * and shifts the other items to make room for the moved item.
     * If there is no free space in the inventory the item is moved to the last index.
     * @param index the index of the item to be shifted.
     */
    fun TransactionOperation.shiftToFreeIndex(index: Int) {
        if (failed) {
            return
        }
        // The last index is invalid
        if (!inventory.inBounds(index + 1)) {
            error = TransactionError.Invalid
            return
        }
        val freeIndex = (index + 1 until inventory.size).firstOrNull { inventory[it].isEmpty() }
        if (freeIndex == null) {
            shift(index, inventory.size - 1)
            return
        }
        // Shift to index of last item as number of items won't change
        shift(index, freeIndex - 1)
    }

    /**
     * Shifts an item from a specified index to another index within the same inventory,
     * and shifts the other items to make room for the moved item.
     * @param fromIndex the index of the item to be shifted.
     * @param toIndex the target index where the item will be moved.
     */
    fun TransactionOperation.shift(fromIndex: Int, toIndex: Int) {
        if (failed || fromIndex == toIndex) {
            return
        }
        if (!inventory.inBounds(fromIndex) || !inventory.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        if (fromIndex < toIndex) {
            for (index in fromIndex until toIndex) {
                val item = inventory[index]
                set(index, inventory[index + 1])
                set(index + 1, item)
            }
        } else {
            for (index in fromIndex downTo toIndex + 1) {
                val item = inventory[index]
                set(index, inventory[index - 1])
                set(index - 1, item)
            }
        }
    }
}
