package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for adding as many items as possible from a container.
 * Adds items to the container until it reaches its capacity or the desired amount is added.
 */
interface AddItemLimit : AddItem {

    /**
     * Adds items to the container until it reaches its capacity or the desired amount is added.
     * @param id the identifier of the item to be added.
     * @param amount the number of items to be added.
     * @return the number of items actually added.
     */
    fun addToLimit(id: String, amount: Int = 1): Int {
        if (failed) {
            return 0
        }
        add(id, amount)
        return when (val error = error) {
            TransactionError.None -> amount
            is TransactionError.Full -> {
                this.error = TransactionError.None
                // Non-stackable items will have already been removed.
                if (container.stackable(id) && error.amount > 0) {
                    add(id, error.amount)
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