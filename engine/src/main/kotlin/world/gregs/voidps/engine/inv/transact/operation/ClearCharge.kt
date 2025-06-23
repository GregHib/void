package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for clearing item charges in an inventory.
 * The clear operation removes all charges from an item in the inventory at a specific index.
 */
object ClearCharge {

    /**
     * Removes all charges of the item at [index].
     * @param index the index of the item in the inventory.
     */
    fun TransactionOperation.discharge(index: Int) {
        val item = inventory[index]
        // Check if is a chargeable item
        if (item.isEmpty() || inventory.stackable(item.id) || !item.def.contains("charges")) {
            error = TransactionError.Invalid
            return
        }
        // Check if there is enough charges to remove
        if (item.value <= 0) {
            error = TransactionError.Deficient(amount = item.value)
            return
        }
        // Clear the charges
        set(index, item.copy(amount = 0))
    }
}
