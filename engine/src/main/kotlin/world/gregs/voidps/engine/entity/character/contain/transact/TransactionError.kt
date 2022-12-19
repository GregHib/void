package world.gregs.voidps.engine.entity.character.contain.transact

/**
 * A sealed class representing errors that can occur during a transaction.
 */
sealed class TransactionError {
    /**
     * An error indicating that an invalid item was encountered during the transaction.
     * Common reasons being index out of bounds or item restriction
     */
    object Invalid : TransactionError()

    /**
     * An error indicating that the container is full and cannot accept any more items.
     * @property quantity The number of items that could be successfully added before running out of space.
     */
    class Full(val quantity: Int) : TransactionError()

    /**
     * An error indicating that the container does not have enough of the item to fulfill the request.
     * @property quantity The number of items that could be successfully removed.
     */
    class Deficient(val quantity: Int = 0) : TransactionError()
}