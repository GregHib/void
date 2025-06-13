package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for swapping two items indices inside an inventory.
 */
object SwapItem {

    /**
     * Swaps the position of two items in the inventory.
     * @param fromIndex the index of the first item in the inventory.
     * @param toIndex the index of the second item in the inventory.
     */
    fun TransactionOperation.swap(fromIndex: Int, toIndex: Int) {
        swap(fromIndex, inventory, toIndex)
    }

    /**
     * Swaps the position of two items in the inventory.
     * @param fromIndex the index of the first item in the inventory.
     * @param toIndex the index of the second item in the inventory.
     */
    fun TransactionOperation.swap(fromIndex: Int, target: Inventory, toIndex: Int) {
        if (failed) {
            return
        }
        if (!inventory.inBounds(fromIndex) || !target.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        val item = inventory[fromIndex]
        val transaction = link(target)
        set(fromIndex, target[toIndex], from = target.id, fromIndex = toIndex)
        transaction.set(toIndex, item, from = inventory.id, fromIndex = fromIndex)
    }
}
