package world.gregs.voidps.engine.contain.transact.operation

/**
 * Transaction operation for clearing items in an inventory.
 * The clear operation removes all items from the inventory, or a specific item if its index is provided.
 */
interface ClearItem : TransactionOperation {

    /**
     * Removes a specific item from the inventory.
     * @param index the index of the item to be removed.
     */
    fun clear(index: Int) {
        if (failed) {
            return
        }
        set(index, null)
    }

    /**
     * Removes all items from the inventory.
     */
    fun clear() {
        if (failed) {
            return
        }
        for (index in inventory.indices) {
            set(index, null)
        }
    }

}
