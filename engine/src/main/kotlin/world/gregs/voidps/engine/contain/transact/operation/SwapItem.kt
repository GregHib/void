package world.gregs.voidps.engine.contain.transact.operation

import world.gregs.voidps.engine.contain.Inventory
import world.gregs.voidps.engine.contain.transact.TransactionError

/**
 * Transaction operation for swapping two items indices inside an inventory.
 */
interface SwapItem : TransactionOperation {

    /**
     * Swaps the position of two items in the inventory.
     * @param fromIndex the index of the first item in the inventory.
     * @param toIndex the index of the second item in the inventory.
     */
    fun swap(fromIndex: Int, toIndex: Int) {
        swap(fromIndex, inventory, toIndex)
    }

    /**
     * Swaps the position of two items in the inventory.
     * @param fromIndex the index of the first item in the inventory.
     * @param toIndex the index of the second item in the inventory.
     */
    fun swap(fromIndex: Int, target: Inventory, toIndex: Int) {
        if (failed) {
            return
        }
        if (!inventory.inBounds(fromIndex) || !target.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        val item = inventory[fromIndex]
        val transaction = link(target)
        set(fromIndex, target[toIndex], to = target.id)
        transaction.set(toIndex, item, from = inventory.id)
    }

}