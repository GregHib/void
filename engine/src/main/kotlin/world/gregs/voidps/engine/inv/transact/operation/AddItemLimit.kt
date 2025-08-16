package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add

/**
 * Transaction operation for adding as many items as possible from an inventory.
 * Adds items to the inventory until it reaches its capacity or the desired amount is added.
 */
object AddItemLimit {

    /**
     * Adds items to the inventory until it reaches its capacity or the desired amount is added.
     * @param id the identifier of the item to be added.
     * @param amount the number of items to be added.
     * @return the number of items actually added.
     */
    fun TransactionOperation.addToLimit(id: String, amount: Int = 1): Int {
        if (failed) {
            return 0
        }
        add(id, amount)
        return when (val error = error) {
            TransactionError.None -> amount
            is TransactionError.Full -> {
                if (error.amount > 0) {
                    this.error = TransactionError.None
                    // Non-stackable items will have already been removed.
                    if (inventory.stackable(id)) {
                        add(id, error.amount)
                        if (!failed) {
                            return error.amount
                        }
                    }
                }
                error.amount
            }
            else -> 0
        }
    }
}
