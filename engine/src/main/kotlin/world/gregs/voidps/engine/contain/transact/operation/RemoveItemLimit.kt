package world.gregs.voidps.engine.contain.transact.operation

import world.gregs.voidps.engine.contain.transact.TransactionError

/**
 * Transaction operation for removing as many items as possible from an inventory.
 * The removeToLimit operation removes items from the inventory until
 * none are remaining or the desired amount is removed.
 */
interface RemoveItemLimit : RemoveItem {

    /**
     * Removes items from the inventory until none are remaining or the desired amount is removed.
     * @param id the identifier of the item to be removed.
     * @param amount the number of items to be removed.
     * @return the number of items actually removed.
     */
    fun removeToLimit(id: String, amount: Int = 1): Int {
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