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
            // Non-stackable items will have already been removed.
            if (container.stackRule.stackable(id) && error.quantity > 0) {
                add(id, error.quantity)
                if (!failed) {
                    return error.quantity
                }
            }
            return error.quantity
        }
        return 0
    }
}