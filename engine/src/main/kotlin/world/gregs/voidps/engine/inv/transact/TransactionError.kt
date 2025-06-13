package world.gregs.voidps.engine.inv.transact

/**
 * A sealed class representing errors that can occur during a transaction.
 */
sealed class TransactionError {
    /**
     * An error indicating that an invalid item was encountered during the transaction.
     * Common reasons being index out of bounds or item restriction
     */
    data object Invalid : TransactionError()

    /**
     * An error indicating that the inventory is full and cannot accept any more items.
     * @property amount The number of items that could be successfully added before running out of space.
     */
    data class Full(val amount: Int = 0) : TransactionError()

    /**
     * An error indicating that the inventory does not have enough of the item to fulfill the request.
     * @property amount The number of items that could be successfully removed.
     */
    data class Deficient(val amount: Int = 0) : TransactionError()

    /**
     * The transaction completed without error or is no longer in progress.
     */
    data object None : TransactionError()
}
