package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.operation.*
import world.gregs.voidps.engine.entity.item.Item

/**
 * TODO Decide what to do about Item.EMPTY - direct access to items should help?
 */
class Transaction(
    private val container: Container
) : TransactionController(), AddItem, AddItemLimit, ClearItem, MoveItem, MoveItemLimit, RemoveItem, RemoveItemLimit, SwapItem {

    override val indices: IntRange
        get() = container.getItems().indices
    override var error: TransactionError? = null
    override val state = StateManager(container.data)
    override val changes = ChangeManager(container)

    override fun indexOfFirst(block: (Item?) -> Boolean): Int {
        return container.getItems().indexOfFirst(block)
    }

    override fun emptyIndex(): Int {
        return container.freeIndex()
    }

    override fun get(index: Int): Item? = container.getItem(index)

    override fun stackable(id: String) = container.stackRule.stack(id)

    override fun checkRemoval(index: Int, quantity: Int) = container.removalCheck.shouldRemove(index, quantity)

    override fun set(index: Int, item: Item?, moved: Boolean) {
        val previous = container.getItem(index)
        changes.track(index, previous, item ?: Item.EMPTY, moved)
        container.set(index, item ?: Item.EMPTY, update = false, moved)
    }

    override fun linkTransaction(container: Container): Transaction {
        val transaction = container.transaction
        if (transaction.state.hasSaved() || transaction.failed || transaction == this) {
            error(TransactionError.Invalid)
            return transaction
        }
        transaction.state.save()
        link(transaction)
        return transaction
    }

    override fun invalid(id: String, quantity: Int): Boolean {
        return !container.isValidInput(id, quantity)
    }

    override fun invalid(index: Int, allowEmpty: Boolean) = invalid(container, index, allowEmpty)

    override fun invalid(container: Container, index: Int, allowEmpty: Boolean): Boolean {
        val item = container.getItem(index)
        if (item.isEmpty()) {
            if (!container.inBounds(index)) {
                return true
            }
            return !allowEmpty
        }
        return invalid(item.id, item.amount)
    }
}