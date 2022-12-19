package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for removing as many items as possible from a container.
 * The removeToLimit operation removes items from the container until
 * none are remaining or the desired quantity is removed.
 */
interface RemoveItemLimit : RemoveItem {

    /**
     * Removes items from the container until none are remaining or the desired quantity is removed.
     * @param id the identifier of the item to be removed.
     * @param quantity the number of items to be removed.
     * @return the number of items actually removed.
     */
    fun removeToLimit(id: String, quantity: Int = 1): Int {
        if (failed) {
            return 0
        }
        remove(id, quantity)
        val error = error ?: return quantity
        if (error == TransactionError.Invalid) {
            return 0
        }
        if (error is TransactionError.Deficient) {
            this.error = null
            // Non-stackable items will have already been removed.
            if (container.stackRule.stackable(id) && error.quantity > 0) {
                remove(id, error.quantity)
                if (!failed) {
                    return error.quantity
                }
            }
            return error.quantity
        }
        return 0
    }
}