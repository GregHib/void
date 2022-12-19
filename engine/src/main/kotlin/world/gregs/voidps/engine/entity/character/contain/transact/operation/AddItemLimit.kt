package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for adding as many items as possible from a container.
 * Adds items to the container until it reaches its capacity or the desired quantity is added.
 */
interface AddItemLimit : AddItem {

    /**
     * Adds items to the container until it reaches its capacity or the desired quantity is added.
     * @param id the identifier of the item to be added.
     * @param quantity the number of items to be added.
     * @return the number of items actually added.
     */
    fun addToLimit(id: String, quantity: Int = 1): Int {
        if (failed) {
            return 0
        }
        add(id, quantity)
        val error = error ?: return quantity
        if (error is TransactionError.Full) {
            this.error = null
            return error.amountAdded
        } else if (error is TransactionError.Overflow) {
            this.error = null
            if (error.remainingSpace > 0) {
                add(id, error.remainingSpace)
                if (!failed) {
                    return error.remainingSpace
                }
            }
        }
        return 0
    }
}