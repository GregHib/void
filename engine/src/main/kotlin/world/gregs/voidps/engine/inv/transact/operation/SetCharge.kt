package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for setting charges of an item in an inventory.
 * Chargeable items charges are set.
 * Non-chargeable items are ignored.
 */
object SetCharge {

    /**
     * Set the charges of the item at the specified index by the given amount.
     * @param index the index of the item charge to be set.
     * @param amount the number of charges to be added.
     */
    fun TransactionOperation.setCharge(index: Int, amount: Int) {
        if (failed) {
            return
        }
        val item = inventory[index]
        if (item.isEmpty() || amount < 0 || inventory.stackable(item.id)) {
            error = TransactionError.Invalid
            return
        }
        val maximum: Int? = item.def.getOrNull("charges_max") ?: item.def.getOrNull("charges")
        if (maximum == null) {
            error = TransactionError.Invalid
            return
        }
        // Check if amount exceeds the maximum value
        if (amount.toLong() > maximum) {
            error = TransactionError.Full(amount - (amount - maximum))
            return
        }
        // Update the item in the inventory
        set(index, item.copy(amount = amount))
    }
}
