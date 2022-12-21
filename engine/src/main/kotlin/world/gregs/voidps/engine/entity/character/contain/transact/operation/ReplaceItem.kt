package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for replacing items in a container.
 * This operation allows replacing an item at a specific index with another item.
 */
interface ReplaceItem : TransactionOperation {

    /**
     * Replaces an item in the container with another item.
     * @param id the identifier of the item to be replaced.
     * @param with the identifier of the item to replace with.
     */
    fun replace(id: String, with: String) {
        replace(container.indexOf(id), id, with)
    }

    /**
     * Replaces an item at the specified index in the container with another item.
     * @param index the index of the item to be replaced.
     * @param id the identifier of the item to be replaced.
     * @param with the identifier of the item to replace with.
     */
    fun replace(index: Int, id: String, with: String) {
        if (failed) {
            return
        }

        val item = container[index]
        if (!container.inBounds(index) || item.id != id || container.restricted(id)) {
            error = TransactionError.Invalid
            return
        }

        set(index, item.copy(id = with))
    }

}