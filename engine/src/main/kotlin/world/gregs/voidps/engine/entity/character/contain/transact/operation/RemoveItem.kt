package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for removing items from a container.
 * Stackable items have their stacks reduced by the quantity required
 * Not stackable items are removed from as many individual slots as required.
 */
interface RemoveItem : TransactionOperation {

    /**
     * Removes an item from the container.
     * @param id the identifier of the item to be removed.
     * @param quantity the number of items to be removed.
     */
    fun remove(id: String, quantity: Int) {
        if (failed) {
            return
        }
        if (quantity <= 0) {
            error = TransactionError.Invalid
            return
        }
        // Check if the item is stackable
        if (!container.stackRule.stackable(id)) {
            removeNonStackableItems(id, quantity)
            return
        }
        // Find the stack of the item and reduce its quantity
        val index = container.indexOf(id)
        if (index != -1) {
            decreaseStack(index, quantity)
            return
        }
        // The item was not found in the container
        error = TransactionError.Deficient()
    }

    /**
     * Decreases the quantity of a stack of items.
     * @param index the index of the stack in the container.
     * @param quantity the number of items to be removed from the stack.
     */
    private fun decreaseStack(index: Int, quantity: Int) {
        val item = container.getItem(index)
        if (item.isEmpty()) {
            error = TransactionError.Invalid
            return
        }
        // Check if there is enough quantity to remove
        if (item.amount < quantity) {
            error = TransactionError.Deficient(quantity = item.amount)
            return
        }
        // Reduce the quantity of the stack
        val combined = item.amount - quantity
        // Remove the stack if its quantity is zero
        if (container.removalCheck.shouldRemove(index, combined)) {
            set(index, null)
        } else {
            set(index, item.copy(amount = combined))
        }
    }

    /**
     * Removes all non-stackable items from the container.
     * @param id the identifier of the non-stackable items to be removed.
     * @param quantity the number of items to be removed.
     */
    private fun removeNonStackableItems(id: String, quantity: Int) {
        // Remove as many non-stackable items as required
        var removed = 0
        for (index in container.items.indices) {
            if (container.getItem(index).id == id) {
                set(index, null)
                // Stop the iteration if the desired number of items have been removed.
                if (++removed == quantity) {
                    return
                }
            }
        }
        // The required quantity of the item was not found
        error = TransactionError.Deficient(quantity = removed)
    }

}
