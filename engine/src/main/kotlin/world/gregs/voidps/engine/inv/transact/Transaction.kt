package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.transact.operation.*

/**
 * Class for performing modification operations on a [inventory].
 * It manages the [state] of the inventory and tracks any [changes] made during the transaction.
 * Any operation [error]s in this or linked [inventory]s will revert all changes upon [commit].
 *
 * Example usage:
 * ```
 * val transaction = inventory.transaction
 * transaction.start()
 * transaction.add("item", amount = 2)
 * transaction.remove("item", amount = 1)
 * val success = transaction.commit()
 * ```
 *
 * @see TransactionController for more info.
 */
class Transaction(
    override val inventory: Inventory,
) : TransactionController(),
    TransactionOperation {

    override var internalError: TransactionError = TransactionError.None
    override var error: TransactionError
        get() = error()
        set(value) {
            internalError = value
        }
    override val state = StateManager(inventory)
    override val changes = ChangeManager(inventory)

    override fun set(index: Int, item: Item?, from: String?, fromIndex: Int?) {
        if (failed) {
            return
        }
        val previous = inventory[index]
        val fromId = from ?: inventory.id
        changes.track(fromId, index, previous, fromIndex ?: index, item ?: Item.EMPTY)
        inventory.items[index] = item ?: Item.EMPTY
    }

    override fun link(inventory: Inventory): Transaction {
        val transaction = inventory.transaction
        if (transaction == this || linked(transaction)) {
            return transaction
        }
        if (transaction.state.hasSaved()) {
            // Inventory has unrelated transaction active
            error = TransactionError.Invalid
            return transaction
        }
        transaction.error = TransactionError.None
        transaction.state.save()
        link(transaction)
        return transaction
    }
}
