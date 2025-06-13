package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for replacing items in an inventory.
 * This operation allows replacing an item at a specific index with another item.
 */
object ReplaceItem {

    /**
     * Replaces an item in the inventory with another item.
     * @param id the identifier of the item to be replaced.
     * @param with the identifier of the item to replace with.
     */
    fun TransactionOperation.replace(id: String, with: String) {
        replace(inventory.indexOf(id), id, with)
    }

    /**
     * Replaces an item at the specified index in the inventory with another item.
     * @param index the index of the item to be replaced.
     * @param id the identifier of the item to be replaced.
     * @param with the identifier of the item to replace with.
     */
    fun TransactionOperation.replace(index: Int, id: String, with: String) {
        if (failed) {
            return
        }

        val item = inventory[index]
        if (!inventory.inBounds(index) || item.id != id || inventory.restricted(id)) {
            error = TransactionError.Invalid
            return
        }

        set(index, item.copy(with))
    }

    /**
     * Replaces an item at the specified index in the inventory with another item.
     * @param index the index of the item to be replaced.
     * @param id the identifier of the item to be replaced.
     * @param with the identifier of the item to replace with.
     */
    fun TransactionOperation.replace(index: Int, id: String, with: String, amount: Int) {
        if (failed) {
            return
        }

        val item = inventory[index]
        if (!inventory.inBounds(index) || item.id != id || inventory.restricted(id)) {
            error = TransactionError.Invalid
            return
        }

        set(index, item.copy(with, amount))
    }
}
