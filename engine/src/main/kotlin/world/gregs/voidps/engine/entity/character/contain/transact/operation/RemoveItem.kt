package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for removing items from a container.
 * Stackable items have their stacks reduced by the amount required
 * Not stackable items are removed from as many individual slots as required.
 */
interface RemoveItem : TransactionOperation {

    /**
     * Removes an item from the container.
     * @param id the identifier of the item to be removed.
     * @param amount the number of items to be removed.
     */
    fun remove(id: String, amount: Int = 1) {
        if (failed) {
            return
        }
        if (amount <= 0) {
            error = TransactionError.Invalid
            return
        }
        // Check if the item is stackable
        if (!container.stackable(id)) {
            removeNonStackableItems(id, amount)
            return
        }
        // Find the stack of the item and reduce its amount
        val index = container.indexOf(id)
        if (index != -1) {
            decreaseStack(index, amount)
            return
        }
        // The item was not found in the container
        error = TransactionError.Deficient()
    }

    /**
     * Decreases the amount in a stack of items.
     * @param index the index of the stack in the container.
     * @param amount the number of items to be removed from the stack.
     */
    private fun decreaseStack(index: Int, amount: Int) {
        val item = container.getItem(index)
        if (item.isEmpty()) {
            error = TransactionError.Invalid
            return
        }
        // Check if there is enough items to remove
        if (item.amount < amount) {
            error = TransactionError.Deficient(amount = item.amount)
            return
        }
        // Reduce the amount in the stack
        val combined = item.amount - amount
        // Remove the stack if its amount is zero
        if (container.needsRemoval(combined, index)) {
            set(index, null)
        } else {
            set(index, item.copy(amount = combined))
        }
    }

    /**
     * Removes all non-stackable items from the container.
     * @param id the identifier of the non-stackable items to be removed.
     * @param amount the number of items to be removed.
     */
    private fun removeNonStackableItems(id: String, amount: Int) {
        // Remove as many non-stackable items as required
        var removed = 0
        for (index in container.indices) {
            if (container.getItem(index).id == id) {
                set(index, null)
                // Stop the iteration if the desired number of items have been removed.
                if (++removed == amount) {
                    return
                }
            }
        }
        // The required amount of the item was not found
        error = TransactionError.Deficient(amount = removed)
    }

}
