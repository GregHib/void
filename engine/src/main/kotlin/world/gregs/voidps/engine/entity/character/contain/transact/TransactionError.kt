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
     * @property amountAdded The amount of items that were added before running out of space.
     */
    class Full(val amountAdded: Int) : TransactionError()

    /**
     * An error indicating that the container does not have enough space to accept the items.
     * @property remainingSpace The amount of space remaining.
     */
    class Overflow(val remainingSpace: Int) : TransactionError()

    /**
     * An error indicating that the container does not have enough of the item to fulfill the request.
     * @property amountRemoved The amount of items that were successfully removed.
     */
    class Deficient(val amountRemoved: Int) : TransactionError()

    /**
     * An error indicating that the item does not have enough quantity to fulfill the request.
     * @property quantity The quantity available.
     */
    class Underflow(val quantity: Int) : TransactionError()

    /**
     * An error indicating that the target container is full and cannot accept any more items.
     */
    object TargetFull : TransactionError()
}