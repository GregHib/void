package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.Transaction
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

/**
 * An interface representing operations that can be performed on a [Container] as part of a [Transaction].
 * These operations may be reversible or undoable, and may involve changes to one or more containers.
 *
 * @property error The error that occurred during the transaction, if any
 * @property failed Whether the transaction has failed
 */
interface TransactionOperation {
    val container: Container
    var error: TransactionError?
    val failed: Boolean
        get() = error != null

    /**
     * Sets the [item] at [index] in the current container
     *
     * @param index The index at which to set the item.
     * @param item The item to set.
     * @param moved Whether the item was moved from
     */
    fun set(index: Int, item: Item?, moved: Boolean = false)

    /**
     * Starts a new transaction for the provided [container] and links it to the current transaction.
     * The completion of the current transaction will be dependent on the success of the linked transaction.
     * [TransactionError.Invalid] [error] when attempting to link a container with an active transaction.
     * @param container the container for which a new transaction should be started and linked to the current transaction
     * @return the newly created and linked transaction
     */
    fun linkTransaction(container: Container): Transaction

    /**
     * Checks if the item [id] and [quantity] are invalid
     *
     * @param id The id of the item to check.
     * @param quantity The amount of the item to check.
     */
    fun invalid(id: String, quantity: Int): Boolean

    /**
     * Checks if the item at [index] is invalid
     *
     * @param index The index of the item to check.
     * @param allowEmpty Whether the index is allowed to be empty
     */
    fun invalid(index: Int, allowEmpty: Boolean = false): Boolean

    /**
     * Checks if the item at [index] in [container] is invalid
     *
     * @param index The index of the item to check.
     * @param container The container in which to check the item.
     * @param allowEmpty Whether the index is allowed to be empty
     */
    fun invalid(container: Container, index: Int, allowEmpty: Boolean = false): Boolean

    /**
     * Sets the [reason] for a transaction error
     *
     * @param reason The reason for the failure of the operation
     */
    fun error(reason: TransactionError) {
        error = reason
    }
}