package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * Transaction operation for removing as many items as possible from an inventory.
 * The removeToLimit operation removes items from the inventory until
 * none are remaining or the desired amount is removed.
 */
object RemoveItemLimit {

    /**
     * Removes items from the inventory until none are remaining or the desired amount is removed.
     * @param id the identifier of the item to be removed.
     * @param amount the number of items to be removed.
     * @return the number of items actually removed.
     */
    fun TransactionOperation.removeToLimit(id: String, amount: Int = 1): Int {
        if (failed) {
            return 0
        }
        remove(id, amount)
        return when (val error = error) {
            TransactionError.None -> amount
            is TransactionError.Deficient -> {
                this.error = TransactionError.None
                // Non-stackable items will have already been removed.
                if (inventory.stackable(id) && error.amount > 0) {
                    remove(id, error.amount)
                    if (!failed) {
                        return error.amount
                    }
                }
                error.amount
            }
            else -> 0
        }
    }
}
