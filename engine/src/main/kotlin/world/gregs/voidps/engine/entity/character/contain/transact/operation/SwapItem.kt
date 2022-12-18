package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError

/**
 * Transaction operation for swapping two items indices inside a container.
 */
interface SwapItem : TransactionOperation {

    /**
     * Swaps the position of two items in the container.
     * @param fromIndex the index of the first item in the container.
     * @param toIndex the index of the second item in the container.
     */
    fun swap(fromIndex: Int, toIndex: Int) {
        if (failed) {
            return
        }
        if (invalid(fromIndex) || invalid(toIndex)) {
            error(TransactionError.Invalid)
            return
        }
        val item = get(fromIndex)
        set(fromIndex, get(toIndex))
        set(toIndex, item)
    }

}