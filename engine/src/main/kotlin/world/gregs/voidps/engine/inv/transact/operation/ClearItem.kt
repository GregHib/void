package world.gregs.voidps.engine.inv.transact.operation

/**
 * Transaction operation for clearing items in an inventory.
 * The clear operation removes all items from the inventory, or a specific item if its index is provided.
 */
object ClearItem {

    /**
     * Removes a specific item from the inventory.
     * @param index the index of the item to be removed.
     */
    fun TransactionOperation.clear(index: Int) {
        if (failed) {
            return
        }
        set(index, null)
    }

    /**
     * Removes all items from the inventory.
     */
    fun TransactionOperation.clear() {
        if (failed) {
            return
        }
        for (index in inventory.indices) {
            set(index, null)
        }
    }
}
