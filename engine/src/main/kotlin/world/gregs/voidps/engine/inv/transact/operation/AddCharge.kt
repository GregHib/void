package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for adding charges to an item in an inventory.
 * Chargeable items charges are increased.
 * Non-chargeable items are ignored.
 */
interface AddCharge : TransactionOperation {

    /**
     * Increases the charges of the item at the specified index by the given amount.
     * @param index the index of the item to be charged.
     * @param amount the number of charges to be added.
     */
    fun charge(index: Int, amount: Int) {
        if (failed) {
            return
        }
        val item = inventory[index]
        if (item.isEmpty() || amount <= 0 || inventory.stackable(item.id)) {
            error = TransactionError.Invalid
            return
        }
        val maximum: Int? = item.def.getOrNull("charges")
        if (maximum == null) {
            error = TransactionError.Invalid
            return
        }
        // Check if the stack would exceed the maximum integer value
        if (item.charges + amount.toLong() > Int.MAX_VALUE) {
            error = TransactionError.Full(Int.MAX_VALUE - item.charges)
            return
        }
        if (item.charges + amount > maximum) {
            error = TransactionError.Full(maximum - item.charges)
            return
        }
        // Combine the charges and update the item in the inventory
        set(index, item.copy(amount = item.charges + amount))
    }

}