package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.operation.*
import world.gregs.voidps.engine.entity.item.Item

/**
 * TODO Decide what to do about Item.EMPTY - direct access to items should help?
 */
class Transaction(
    override val container: Container
) : TransactionController(), AddItem, AddItemLimit, ClearItem, MoveItem, MoveItemLimit, RemoveItem, RemoveItemLimit, ReplaceItem, SwapItem {

    override var error: TransactionError? = null
    override val state = StateManager(container.data)
    override val changes = ChangeManager(container)

    override fun set(index: Int, item: Item?, moved: Boolean) {
        if (failed) {
            return
        }
        val previous = container.getItem(index)
        changes.track(index, previous, item ?: Item.EMPTY, moved)
        container.set(index, item ?: Item.EMPTY, update = false, moved)
    }

    override fun linkTransaction(container: Container): Transaction {
        val transaction = container.transaction
        if (transaction == this || linked(transaction)) {
            return transaction
        }
        if (transaction.state.hasSaved() || transaction.failed) {
            // Container has unrelated transaction active
            error(TransactionError.Invalid)
            return transaction
        }
        transaction.state.save()
        link(transaction)
        return transaction
    }

    override fun invalid(id: String, quantity: Int): Boolean {
        return id.isBlank() || quantity <= container.removalCheck.getMinimum() || container.itemRule.restricted(id, quantity) || !container.definitions.contains(id)
    }

    override fun invalid(index: Int): Boolean {
        if (!container.inBounds(index)) {
            return true
        }
        val item = container.getItem(index)
        if (item.isEmpty()) {
            return true
        }
        return invalid(item.id, item.amount)
    }
}