package world.gregs.voidps.engine.inv.transact

import com.github.michaelbull.logging.InlineLogger

/**
 * The transaction controller handles starting and stopping the transaction,
 * Committing or reverting a transaction will update the state of
 * the inventory and its linked transactions.
 */
abstract class TransactionController {

    protected abstract var internalError: TransactionError
    private val transactions: MutableSet<Transaction> = mutableSetOf()
    abstract val state: StateManager
    abstract val changes: ChangeManager

    /**
     * Function to start the transaction.
     * Resets the transaction and saves the inventory state
     */
    fun start() {
        internalError = TransactionError.None
        reset()
        state.save()
    }

    /**
     * Link a transaction to this transaction.
     * @param transaction the transaction to be linked to this transaction
     */
    fun link(transaction: Transaction) {
        transactions.add(transaction)
    }

    /**
     * Check if a transaction is linked to this transaction.
     * @param transaction the transaction to check if is linked
     * @return a boolean indicating whether the transaction is linked
     */
    fun linked(transaction: Transaction): Boolean = transactions.contains(transaction)

    /**
     * Function to revert the transaction to the last saved state.
     * Revert the transaction and any linked transactions to their last saved state, and removing the saved state.
     * @return a boolean indicating whether the revert was successful
     */
    fun revert(): Boolean {
        internalError = error()
        val success = state.revert()
        if (!success) {
            logger.warn { "Failed to revert transaction $this." }
        }
        for (transaction in transactions) {
            if (!transaction.state.revert()) {
                throw IllegalStateException("Failed to revert history for transaction $transaction.")
            }
        }
        resetAll()
        return success
    }

    /**
     * @return the first error from any linked transactions
     */
    protected fun error() = transactions.fold(internalError) { e, txn -> if (e != TransactionError.None) e else txn.error }

    /**
     * Permanently applies the changes made to the inventories during the transaction.
     * If an error occurs during the transaction, the transaction and its linked transactions are reverted to the last saved state.
     * @return a boolean indicating whether the commit was successful
     */
    fun commit(): Boolean {
        internalError = error()
        if (internalError != TransactionError.None) {
            revert()
            return false
        }
        sendChanges()
        resetAll()
        return true
    }

    /**
     * Sends the changes made during the transaction to the appropriate recipients.
     * This includes sending the changes for the linked transactions.
     */
    private fun sendChanges() {
        transactions.forEach { txn ->
            txn.changes.send()
        }
        changes.send()
    }

    /**
     * Resets the transaction and its linked transactions.
     */
    private fun resetAll() {
        transactions.forEach(Transaction::reset)
        reset()
    }

    protected fun reset() {
        state.clear()
        changes.clear()
        transactions.clear()
    }

    companion object {
        private val logger = InlineLogger()
    }
}
