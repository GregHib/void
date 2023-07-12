package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.transact.operation.*
import world.gregs.voidps.engine.entity.item.Item

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
    override val inventory: Inventory
) : TransactionController(), AddItem, AddItemLimit, ClearItem, MoveItem, MoveItemLimit, RemoveItem, RemoveItemLimit, ReplaceItem, ShiftItem, SwapItem {

    override var error: TransactionError = TransactionError.None
    override val state = StateManager(inventory)
    override val changes = ChangeManager(inventory)

    override fun set(index: Int, item: Item?, from: String?, to: String?) {
        if (failed) {
            return
        }
        val previous = inventory[index]
        val fromId = from ?: inventory.id
        val toId = to ?: inventory.id
        changes.track(fromId, index, previous, toId, item ?: Item.EMPTY)
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