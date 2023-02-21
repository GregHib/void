package world.gregs.voidps.engine.contain.transact.operation

import world.gregs.voidps.engine.contain.Container
import world.gregs.voidps.engine.contain.transact.Transaction
import world.gregs.voidps.engine.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

/**
 * Operations that can be performed on a [Container] as part of a [Transaction].
 * These operations may be reversible or undoable, and may involve changes to one or more containers.
 *
 * @property error The error that occurred during the transaction, if any
 * @property failed Whether the transaction has failed
 */
interface TransactionOperation {
    val container: Container
    var error: TransactionError
    val failed: Boolean
        get() = error != TransactionError.None

    /**
     * Sets the [item] at [index] in the current container
     *
     * @param index The index at which to set the item.
     * @param item The item to set.
     * @param from Which container the item was moved from
     * @param to Which container the item was moved to
     */
    fun set(index: Int, item: Item?, from: String? = null, to: String? = null)

    /**
     * Starts a new transaction for the provided [container] and links it to the current transaction.
     * The completion of the current transaction will be dependent on the success of the linked transaction.
     * [TransactionError.Invalid] [error] when attempting to link a container with an active transaction.
     * @param container the container for which a new transaction should be started and linked to the current transaction
     * @return the newly created and linked transaction
     */
    fun link(container: Container): Transaction

}