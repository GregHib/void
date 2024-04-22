package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for removing charges from an item in an inventory.
 * Chargeable items have their charge reduced by the amount required
 * Non-chargeable items are ignored.
 */
object RemoveCharge {

    /**
     * Decreases the charges of an item at [index].
     * @param index the index of the item in the inventory.
     * @param amount the number of charges to be removed from the item.
     */
    fun TransactionOperation.discharge(index: Int, amount: Int) {
        if (failed) {
            return
        }
        val item = inventory[index]
        if (item.isEmpty() || amount <= 0 || inventory.stackable(item.id)) {
            error = TransactionError.Invalid
            return
        }
        if (!item.def.contains("charges")) {
            error = TransactionError.Invalid
            return
        }
        // Check if there is enough charges to remove
        if (item.value < amount) {
            error = TransactionError.Deficient(amount = item.value)
            return
        }
        // Reduce the charges in the stack
        val combined = item.value - amount
        set(index, item.copy(amount = combined))
    }

}
