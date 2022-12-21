package world.gregs.voidps.engine.entity.character.contain.transact.operation

/**
 * Transaction operation for clearing items in a container.
 * The clear operation removes all items from the container, or a specific item if its index is provided.
 */
interface ClearItem : TransactionOperation {

    /**
     * Removes a specific item from the container.
     * @param index the index of the item to be removed.
     */
    fun clear(index: Int) {
        if (failed) {
            return
        }
        set(index, null)
    }

    /**
     * Removes all items from the container.
     */
    fun clear() {
        if (failed) {
            return
        }
        for (index in container.indices) {
            set(index, null)
        }
    }

}
