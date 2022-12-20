package world.gregs.voidps.engine.entity.character.contain

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.remove.ItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.entity.character.contain.restrict.NoRestrictions
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.ItemStackingRule
import world.gregs.voidps.engine.entity.character.contain.transact.Transaction
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import java.util.*

data class Container(
    internal val data: ContainerData,
    var id: String = "",
    var itemRule: ItemRestrictionRule = NoRestrictions,
    val stackRule: ItemStackingRule = AlwaysStack,
    val removalCheck: ItemRemovalChecker = DefaultItemRemovalChecker,
    val events: MutableSet<Events> = mutableSetOf()
) {

    val items: Array<Item>
        get() = data.items

    var capacity: Int = items.size

    lateinit var definitions: ItemDefinitions

    private var updates = mutableListOf<ItemChanged>()

    var result: ContainerResult = ContainerResult.Success
        private set


    private fun result(result: ContainerResult): Boolean {
        this.result = result
        return result == ContainerResult.Success
    }

    val transaction: Transaction by lazy { Transaction(this) }

    fun transaction(block: Transaction.() -> Unit): Boolean {
        transaction.start()
        block.invoke(transaction)
        return transaction.commit()
    }

    fun stackable(id: String) = stackRule.stackable(id)

    val count: Int
        get() = items.indices.count { !isIndexFree(it) }

    val spaces: Int
        get() = items.indices.count { isIndexFree(it) }

    fun isEmpty() = items.indices.all { isIndexFree(it) }

    fun isFull() = items.indices.none { isIndexFree(it) }

    fun isNotFull() = items.indices.any { isIndexFree(it) }

    fun getItemId(index: Int): String = items.getOrNull(index)?.id ?: ""

    fun getItem(index: Int): Item = items.getOrNull(index) ?: Item("", getMinimum(index))

    fun getAmount(index: Int): Int = items.getOrNull(index)?.amount ?: 0

    private fun getMinimum(index: Int): Int = removalCheck.getMinimum(index)

    @JvmName("items")
    fun getItems(): Array<Item> = items.clone()

    fun indexOf(id: String) = if (id.isBlank()) -1 else items.indexOfFirst { it.id == id }

    fun contains(id: String) = indexOf(id) != -1

    fun contains(id: String, amount: Int): Boolean {
        if (!stackable(id)) {
            return getCount(id) >= amount
        }
        val index = indexOf(id)
        if (index == -1) {
            return false
        }
        return getAmount(index) >= amount
    }

    fun inBounds(index: Int) = index in items.indices

    fun isValid(index: Int, id: String, amount: Int) = isValidId(index, id) && isValidAmount(index, amount)

    fun isValidId(index: Int, id: String) = inBounds(index) && items[index].id == id

    fun isValidAmount(index: Int, amount: Int) = inBounds(index) && items[index].amount == amount

    fun isValidInput(id: String, amount: Int): Boolean {
        return !itemRule.restricted(id) && removalCheck.exceedsMinimum(amount)
    }

    private fun isValidInput(id: String, amount: Int, index: Int): Boolean {
        return !itemRule.restricted(id) && removalCheck.exceedsMinimum(amount, index)
    }

    /**
     * Checks [amount] for a slot is empty
     */
    private fun isFree(index: Int, amount: Int) = removalCheck.shouldRemove(index, amount)

    /**
     * If values is underflowing [minimumAmounts]
     */
    private fun isUnderMin(index: Int, amount: Int) = amount < getMinimum(index)

    /**
     * Checks if an index is free
     */
    fun isIndexFree(index: Int) = isFree(index, items[index].amount)

    fun freeIndex(): Int {
        for (index in items.indices) {
            if (isIndexFree(index)) {
                return index
            }
        }
        return -1
    }

    fun getCount(item: Item) = getCount(item.id)

    fun getCount(id: String): Long {
        var count = 0L
        if (id.isBlank()) {
            return count
        }
        for (index in items.indices) {
            if (getItemId(index) == id && getAmount(index) > getMinimum(index)) {
                count += getAmount(index)
            }
        }
        return count
    }

    /**
     * Clears item at the given index
     * @return successful
     */
    fun clear(index: Int, update: Boolean = true, moved: Boolean = false): Boolean = set(index, "", getMinimum(index), update, moved)

    /**
     * Clears all indices
     */
    fun clearAll() {
        repeat(items.size) { index ->
            clear(index, false)
        }
        update()
    }

    fun set(index: Int, id: String, amount: Int = 1, update: Boolean = true, moved: Boolean = false): Boolean {
        return set(index, Item(id, amount, def = definitions.get(id)), update, moved)
    }

    fun set(index: Int, item: Item, update: Boolean = true, moved: Boolean = false): Boolean {
        if (!inBounds(index)) {
            return false
        }
        val previous = getItem(index)
        track(index, previous, item, moved)
        items[index] = item
        if (update) {
            update()
        }
        return true
    }

    /**
     * Adds items at a specific index
     * Note: Will never add items outside the given [index]
     * @param id The item to add
     * @param amount The stack amount or individual count
     * @param moved If this action is part of a larger movement transaction
     * @param coerce Limit amount to container [spaces] and ignore the overflow
     * @return Whether an item was successfully added
     */
    fun add(index: Int, id: String, amount: Int = 1, moved: Boolean = false, coerce: Boolean = false): Boolean {
        if (!inBounds(index) || !isValidInput(id, amount, index)) {
            return result(ContainerResult.Invalid)
        }

        val item = items[index]
        if (item.id.isNotBlank() && item.id != id) {
            return result(ContainerResult.WrongType)
        }

        val stack = item.amount
        val combined = stack + amount

        if (combined > 1 && !stackable(id)) {
            return add(id, amount, coerce = coerce)
        }

        if (overflows(stack, combined, amount)) {
            if (coerce) {
                return set(index, id, Int.MAX_VALUE, moved = moved)
            }
            return result(ContainerResult.Overflow)
        }

        set(index, id, combined, moved = moved)
        return result(ContainerResult.Success)
    }

    /**
     * Adds any number of items stacked or otherwise
     * @param id The id of the item(s) to add
     * @param amount The stack amount or individual count
     * @param moved If this action is part of a movement transaction
     * @param coerce Limit amount to container [spaces] and ignore the overflow
     * @return Whether an item was successfully added
     */
    fun add(id: String, amount: Int = 1, moved: Boolean = false, coerce: Boolean = false): Boolean {
        if (!isValidInput(id, amount)) {
            return result(ContainerResult.Invalid)
        }
        if (stackable(id)) {
            var index = indexOf(id)
            if (index != -1) {
                val stack = items[index].amount
                val combined = stack + amount
                if (overflows(stack, combined, amount)) {
                    if (coerce) {
                        return set(index, id, Int.MAX_VALUE, moved = moved)
                    }
                    return result(ContainerResult.Overflow)
                }
                set(index, id, combined, moved = moved)
            } else {
                index = freeIndex()
                if (index == -1) {
                    return result(ContainerResult.Full)
                }

                set(index, id, amount, moved = moved)
            }
        } else {
            var amount = amount
            if (coerce) {
                amount = amount.coerceAtMost(spaces)
            }
            if (spaces < amount) {
                return result(ContainerResult.Full)
            }
            repeat(amount) {
                val index = freeIndex()
                set(index, id, amount = 1, update = false, moved = moved)
            }
            update()
        }
        return result(ContainerResult.Success)
    }

    private fun overflows(stack: Int, combined: Int, amount: Int) = stack xor combined and (amount xor combined) < 0

    fun sortedByDescending(block: (Item) -> Int) {
        val all = this.items.sortedByDescending(block)
        all.forEachIndexed { index, item ->
            this.items[index] = item
        }
    }

    fun sort() {
        val all = LinkedList<Item>()
        for ((index, item) in this.items.withIndex().reversed()) {
            if (isFree(index, item.amount)) {
                all.addLast(item)
            } else {
                all.addFirst(item)
            }
        }
        all.forEachIndexed { index, item ->
            this.items[index] = item
        }
    }

    private fun track(index: Int, oldItem: Item, item: Item, moved: Boolean) {
        updates.add(ItemChanged(id, index, oldItem, item, moved))
    }

    private fun update() {
        for (events in events) {
            events.emit(ContainerUpdate(container = id, updates = updates))
            for (update in updates) {
                events.emit(update)
            }
        }
        updates = mutableListOf()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Container

        if (stackRule != other.stackRule) return false
        if (!items.contentEquals(other.items)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = stackRule.hashCode()
        result = 31 * result + items.contentHashCode()
        return result
    }

    companion object {
        private val logger = InlineLogger()

        fun debug(
            capacity: Int,
            itemRule: ItemRestrictionRule = NoRestrictions,
            stackRule: ItemStackingRule = AlwaysStack,
            id: String = "",
            removalCheck: ItemRemovalChecker = DefaultItemRemovalChecker,
        ) = Container(
            ContainerData(Array(capacity) { Item("", removalCheck.getMinimum(it), def = ItemDefinition.EMPTY) }),
            id,
            itemRule,
            stackRule,
            removalCheck
        )
    }
}