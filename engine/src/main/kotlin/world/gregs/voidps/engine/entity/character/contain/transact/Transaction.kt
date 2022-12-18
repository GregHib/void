package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.ItemChanged
import world.gregs.voidps.engine.entity.character.contain.transact.operation.*
import world.gregs.voidps.engine.entity.item.Item
import java.util.*

class Transaction(
    private val container: Container
) : AddItem, AddItemLimit, ClearItem, MoveItem, MoveItemLimit, RemoveItem, RemoveItemLimit, SwapItem {
    override var error: TransactionError? = null
    override val indices: IntRange = container.getItems().indices
    private val histories: MutableMap<Container, Array<Item>> = mutableMapOf(container to container.getItems().copyOf())
    // TODO decide how to handle changes on other containers
    //  if the other container is external (owned by another player) then only external.txn {} should be used so changes are send
    //  but what if we remove changes from the container class?
    //  will need to either stop external containers using [set] or convert all external.txn {} calls to transaction ones and handle changes per [Events]
    private val changes: Stack<ItemChanged> = Stack()

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
        container.set(index, item ?: Item.EMPTY, moved)
    }

    override fun set(container: Container, index: Int, item: Item?, moved: Boolean) {
        val previous = container.getItem(index)
        changes.add(ItemChanged(container.id, index, previous, item ?: Item.EMPTY, moved))
        container.set(index, item ?: Item.EMPTY, update = false, moved)
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

    fun marked(container: Container): Boolean {
        return histories.containsKey(container)
    }

    override fun mark(container: Container) {
        if (!marked(container)) {
            histories[container] = container.getItems().copyOf()
        }
    }

    fun revert() {
        for ((container, history) in histories) {
            container.setItems(history)
        }
    }

    fun commit(): Boolean {
        if (failed) {
            revert()
            histories.clear()
            return false
        }
        for (events in container.events) {
            for (change in changes) {
                events.emit(change)
            }
        }
        histories.clear()
        return true
    }

}