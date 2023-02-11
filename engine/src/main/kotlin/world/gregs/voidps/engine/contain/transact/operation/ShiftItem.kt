package world.gregs.voidps.engine.contain.transact.operation

import world.gregs.voidps.engine.contain.transact.TransactionError

/**
 * Transaction operation for shifting items within a container.
 * This operation allows shifting an item from one index to another within the same container,
 * and shifting the other items to make room for the moved item.
 */
interface ShiftItem : TransactionOperation {

    /**
     * Shifts an item from a specified index to the next free index of the container,
     * and shifts the other items to make room for the moved item.
     * If there is no free space in the container the item is moved to the last index.
     * @param index the index of the item to be shifted.
     */
    fun shiftToFreeIndex(index: Int) {
        if (failed) {
            return
        }
        // The last index is invalid
        if (!container.inBounds(index + 1)) {
            error = TransactionError.Invalid
            return
        }
        val freeIndex = (index + 1 until container.size).firstOrNull { container[it].isEmpty() }
        if (freeIndex == null) {
            shift(index, container.size - 1)
            return
        }
        // Shift to index of last item as number of items won't change
        shift(index, freeIndex - 1)
    }

    /**
     * Shifts an item from a specified index to another index within the same container,
     * and shifts the other items to make room for the moved item.
     * @param fromIndex the index of the item to be shifted.
     * @param toIndex the target index where the item will be moved.
     */
    fun shift(fromIndex: Int, toIndex: Int) {
        if (failed || fromIndex == toIndex) {
            return
        }
        if (!container.inBounds(fromIndex) || !container.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        if (fromIndex < toIndex) {
            for (index in fromIndex until toIndex) {
                val item = container[index]
                set(index, container[index + 1])
                set(index + 1, item)
            }
        } else {
            for (index in fromIndex downTo toIndex + 1) {
                val item = container[index]
                set(index, container[index - 1])
                set(index - 1, item)
            }
        }
    }

}