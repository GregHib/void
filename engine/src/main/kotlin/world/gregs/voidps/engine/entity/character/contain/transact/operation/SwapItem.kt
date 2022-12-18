package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
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
        if (invalid(fromIndex, allowEmpty = true) || invalid(toIndex, allowEmpty = true)) {
            error(TransactionError.Invalid)
            return
        }
        val item = container.getItem(fromIndex)
        set(fromIndex, container.getItem(toIndex))
        set(toIndex, item)
    }

    /**
     * TODO finish -> unit tests - should merge if ids are the same - maybe needs another name?
     * Swaps the position of two items in the container.
     * @param fromIndex the index of the first item in the container.
     * @param toIndex the index of the second item in the container.
     */
    fun swap(fromIndex: Int, target: Container, toIndex: Int) {
        if (failed) {
            return
        }
        if (invalid(fromIndex, allowEmpty = true) || invalid(target, toIndex, allowEmpty = true)) {
            error(TransactionError.Invalid)
            return
        }
        val item = container.getItem(fromIndex)
        val transaction = linkTransaction(target)
        set(fromIndex, container.getItem(toIndex))
        transaction.set(toIndex, item)
    }

}