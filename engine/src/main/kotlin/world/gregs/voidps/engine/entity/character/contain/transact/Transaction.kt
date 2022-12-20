package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.operation.*
import world.gregs.voidps.engine.entity.item.Item

/**
 * Class for performing modification operations on a [container].
 * It manages the [state] of the container and tracks any [changes] made during the transaction.
 * Any operation [error]s in this or linked [container]s will revert all changes upon [commit].
 *
 * Example usage:
 * ```
 * val transaction = container.transaction
 * transaction.start()
 * transaction.add("item", amount = 2)
 * transaction.remove("item", amount = 1)
 * val success = transaction.commit()
 * ```
 *
 * @see TransactionController for more info.
 */
class Transaction(
    override val container: Container
) : TransactionController(), AddItem, AddItemLimit, ClearItem, MoveItem, MoveItemLimit, RemoveItem, RemoveItemLimit, ReplaceItem, ShiftInsertItem, SwapItem {

    override var error: TransactionError = TransactionError.None
    override val state = StateManager(container.data)
    override val changes = ChangeManager(container)

    override fun set(index: Int, item: Item?, moved: Boolean) {
        if (failed) {
            return
        }
        val previous = container.getItem(index)
        changes.track(index, previous, item ?: Item.EMPTY, moved)
        container.data.items[index] = item ?: Item.EMPTY
    }

    override fun link(container: Container): Transaction {
        val transaction = container.transaction
        if (transaction == this || linked(transaction)) {
            return transaction
        }
        if (transaction.state.hasSaved() || transaction.failed) {
            // Container has unrelated transaction active
            error = TransactionError.Invalid
            return transaction
        }
        transaction.state.save()
        link(transaction)
        return transaction
    }
}