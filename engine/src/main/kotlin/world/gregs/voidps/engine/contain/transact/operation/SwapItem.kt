package world.gregs.voidps.engine.contain.transact.operation

import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.contain.transact.TransactionError

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
        swap(fromIndex, container, toIndex)
    }

    /**
     * Swaps the position of two items in the container.
     * @param fromIndex the index of the first item in the container.
     * @param toIndex the index of the second item in the container.
     */
    fun swap(fromIndex: Int, target: Container, toIndex: Int) {
        if (failed) {
            return
        }
        if (!container.inBounds(fromIndex) || !target.inBounds(toIndex)) {
            error = TransactionError.Invalid
            return
        }
        val item = container[fromIndex]
        val transaction = link(target)
        set(fromIndex, target[toIndex], to = target.id)
        transaction.set(toIndex, item, from = container.id)
    }

}