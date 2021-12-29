package world.gregs.voidps.engine.entity.character.contain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.utility.get
import java.util.*

data class Container(
    private val items: Array<Item>
) {

    @JsonIgnore
    lateinit var minimumAmounts: IntArray

    @JsonIgnore
    lateinit var id: String

    @JsonIgnore
    var capacity: Int = items.size

    @JsonIgnore
    var stackMode: StackMode = StackMode.Normal

    @JsonIgnore
    lateinit var definitions: ItemDefinitions

    @JsonIgnore
    val events = mutableSetOf<Events>()

    @JsonIgnore
    var secondary: Boolean = false

    @JsonIgnore
    private var updates = mutableListOf<ItemChanged>()

    @JsonIgnore
    var result: ContainerResult = ContainerResult.Success
        private set

    @JsonIgnore
    var setup = false

    /**
     * A predicate to check if an item is allowed to be added to this container.
     */
    @JsonIgnore
    var predicate: ((String, Int) -> Boolean)? = null

    private fun result(result: ContainerResult): Boolean {
        this.result = result
        return result == ContainerResult.Success
    }

    fun stackable(id: String) = when (stackMode) {
        StackMode.Always -> true
        StackMode.Never -> false
        StackMode.Normal -> definitions.get(id).stackable == 1
    }

    @get:JsonIgnore
    val count: Int
        get() = items.indices.count { !isIndexFree(it) }

    @get:JsonIgnore
    val spaces: Int
        get() = items.indices.count { isIndexFree(it) }

    @JsonIgnore
    fun isEmpty() = items.indices.all { isIndexFree(it) }

    @JsonIgnore
    fun isFull() = items.indices.none { isIndexFree(it) }

    @JsonIgnore
    fun isNotFull() = items.indices.any { isIndexFree(it) }

    fun getItemId(index: Int): String = items.getOrNull(index)?.id ?: ""

    fun getItem(index: Int): Item = items.getOrNull(index) ?: Item("", getMinimum(index))

    fun getAmount(index: Int): Int = items.getOrNull(index)?.amount ?: 0

    private fun getMinimum(index: Int): Int = minimumAmounts.getOrNull(index) ?: 0

    fun getItems(): Array<Item> = items.clone()

    fun indexOf(id: String) = if (id.isBlank()) -1 else items.indexOfFirst { it.id == id }

    fun contains(id: String) = indexOf(id) != -1

    fun contains(id: String, amount: Int): Boolean {
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
        return isValidId(id) && isValidAmount(amount) && definitions.contains(id) && (predicate == null || predicate!!.invoke(id, amount))
    }

    private fun isValidInput(id: String, amount: Int, index: Int): Boolean {
        return isValidId(id) && isValidAmountIndex(amount, index) && definitions.contains(id) && (predicate == null || predicate!!.invoke(id, amount))
    }

    fun isValidOrEmpty(item: Item, index: Int) = (!isValidId(item.id) && !isValidAmountIndex(item.amount, index)) || isValidInput(item, index)

    private fun isValidInput(item: Item, index: Int): Boolean {
        return isValidId(item.id) && isValidAmountIndex(item.amount, index) && item.def.id != -1 && (predicate == null || predicate!!.invoke(item.id, item.amount))
    }

    private fun isValidId(id: String) = id.isNotBlank()

    private fun isValidAmount(amount: Int) = amount > 0

    private fun isValidAmountIndex(amount: Int, index: Int) = amount > getMinimum(index)

    /**
     * Checks [amount] for a slot is empty
     */
    private fun isFree(index: Int, amount: Int) = amount == getMinimum(index)

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
        return set(index, Item(id, amount), update, moved)
    }

    private fun set(index: Int, item: Item, update: Boolean = true, moved: Boolean = false): Boolean {
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
     * Swaps two indices
     * @return Whether the indices were swapped
     */
    fun swap(firstIndex: Int, secondIndex: Int): Boolean {
        if (!inBounds(firstIndex) || !inBounds(secondIndex)) {
            return false
        }
        val temp = getItem(firstIndex)
        set(firstIndex, getItem(secondIndex), update = false, moved = true)
        set(secondIndex, temp, update = false, moved = true)
        update()
        return true
    }

    /**
     * Swaps two indices in different containers
     * @return Whether the indices were swapped
     * @param combine Move the items if they match
     */
    fun swap(firstIndex: Int, container: Container, secondIndex: Int, combine: Boolean = false): Boolean {
        if (!inBounds(firstIndex) || !inBounds(secondIndex)) {
            result(ContainerResult.Invalid)
            container.result(ContainerResult.Invalid)
            return false
        }
        val from = items[firstIndex]
        val to = container.items[secondIndex]
        if (!isValidOrEmpty(to, secondIndex) || !container.isValidOrEmpty(from, firstIndex)) {
            result(ContainerResult.Invalid)
            container.result(ContainerResult.Invalid)
            return false
        }
        if (combine && from.id == to.id && container.stackable(to.id)) {
            return move(firstIndex, container, secondIndex)
        }
        set(firstIndex, to, moved = true)
        container.set(secondIndex, from, moved = true)
        return true
    }

    /**
     * Replaces one unstackable item with another
     * @param id The item id to replace
     * @param replacement The replacement item id
     * @return Whether the item was found and replaced successfully
     */
    fun replace(id: String, replacement: String): Boolean {
        if (stackable(id) || stackable(replacement)) {
            return false
        }
        val index = indexOf(id)
        if (index == -1) {
            return false// Not found
        }
        set(index, replacement, 1)
        return true
    }

    /**
     * Replaces one unstackable item with another
     * @param id The item id to replace
     * @param replacement The replacement item id
     * @return Whether the item was found and replaced successfully
     */
    fun replace(index: Int, id: String, replacement: String): Boolean {
        if (stackable(id) || stackable(replacement)) {
            return false
        }
        if (!inBounds(index)) {
            return result(ContainerResult.Invalid)
        }
        set(index, replacement, 1)
        return true
    }

    /**
     * Inserts between items at a specific index
     * @param index The index to insert at
     * @param id The item to add
     * @param amount The stack amount or individual count
     *  @param moved If this action is part of a larger movement transaction
     * @return Whether an item was successfully inserted
     */
    fun insert(index: Int, id: String, amount: Int = 1, moved: Boolean = false): Boolean {
        if (!inBounds(index) || !isValidInput(id, amount, index)) {
            return result(ContainerResult.Invalid)
        }

        if (amount > 1 && !stackable(id)) {
            return result(ContainerResult.Unstackable)
        }

        val free = freeIndex()
        if (free == -1) {
            return result(ContainerResult.Full)
        }

        for (i in free downTo index + 1) {
            set(i, items[i - 1], update = false)
        }
        set(index, id, amount, moved = moved)
        return result(ContainerResult.Success)
    }

    /**
     * Adds items at a specific index
     * Note: Will never add items outside of the given [index]
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

        if (stack xor combined and (amount xor combined) < 0) {
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
                if (stack xor combined and (amount xor combined) < 0) {
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

    /**
     *  Removes items from a specific container index
     *  Note: Will never remove items not in this index
     *  @param id The item id to remove
     *  @param amount The stack number to remove (default 1 for unstackable)
     *  @param moved If this action is part of a movement transaction
     *  @return Whether an item was successfully removed
     */
    fun remove(index: Int, id: String, amount: Int = 1, moved: Boolean = false): Boolean {
        if (!inBounds(index) || !isValidInput(id, amount, index)) {
            return result(ContainerResult.Invalid)
        }

        val item = items[index]
        if (item.id != id) {
            return result(ContainerResult.WrongType)
        }

        if (amount > 1 && !stackable(id)) {
            return remove(id, amount, moved)
        }

        checkCombined(index, amount)?.let {
            return result(it)
        }
        val combined = item.amount - amount

        if (combined > 1 && !stackable(id)) {
            return result(ContainerResult.Unstackable)
        }

        set(index, id, combined, moved = moved)
        return result(ContainerResult.Success)
    }

    /**
     *  Removes any number of items stacked or otherwise
     *  @param id The item id to remove
     *  @param amount The stack or individual number of items to remove
     *  @param moved If this action is part of a movement transaction
     *  @return Whether an item was successfully removed
     */
    fun remove(id: String, amount: Int = 1, moved: Boolean = false): Boolean {
        if (!isValidInput(id, amount)) {
            return result(ContainerResult.Invalid)
        }
        var index = indexOf(id)
        if (index == -1) {
            return result(ContainerResult.Deficient)
        }

        if (stackable(id)) {
            checkCombined(index, amount)?.let {
                return result(it)
            }
            val combined = items[index].amount - amount
            set(index, id, combined, moved = moved)
        } else {
            val count = items.count { it.id == id }
            if (count < amount) {
                return result(ContainerResult.Deficient)
            }

            repeat(amount) {
                index = indexOf(id)
                clear(index, update = false, moved = moved)
            }
            update()
        }
        return result(ContainerResult.Success)
    }

    private fun checkCombined(index: Int, amount: Int): ContainerResult? {
        val stack = items[index].amount
        val combined = stack - amount

        if (stack xor amount and (stack xor combined) < 0) {
            return ContainerResult.Deficient
        }

        if (isUnderMin(index, combined)) {
            return ContainerResult.Deficient
        }

        if (isFree(index, combined)) {
            clear(index)
            return ContainerResult.Success
        }
        return null
    }

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

    fun moveAll(other: Container, targetIndex: Int? = null, insert: Boolean = false): Boolean {
        var success = true
        for (index in items.indices.reversed()) {
            if (!isIndexFree(index)) {
                if (!move(index, other, targetIndex, insert)) {
                    success = false
                    break
                }
            }
        }
        return success
    }

    fun move(index: Int, container: Container, targetIndex: Int? = null, insert: Boolean = false): Boolean {
        val id = getItemId(index)
        val amount = getAmount(index)
        if (id.isBlank() || amount == getMinimum(index)) {
            return result(ContainerResult.Invalid)
        }
        return move(container, id, amount, index, targetIndex, insert)
    }

    /**
     * Moves item from one container to another
     * Note: In a max-amount scenario it is possible that move returns false even though items have been moved.
     */
    fun move(
        container: Container,
        id: String,
        amount: Int = 1,
        index: Int? = null,
        targetIndex: Int? = null,
        insert: Boolean = false,
        targetId: String = id
    ): Boolean {
        var success = if (index == null) {
            remove(id, amount, moved = true)
        } else {
            remove(index, id, amount, moved = true)
        }

        if (!success) {
            return success
        }

        success = if (targetIndex == null) {
            container.add(targetId, amount, moved = true)
        } else {
            if (insert) {
                container.insert(targetIndex, targetId, amount, moved = true)
            } else {
                container.add(targetIndex, targetId, amount, moved = true)
            }
        }

        if (success) {
            return result(container.result)
        }

        val result = container.result

        if (result == ContainerResult.Overflow) {
            return gracefullyOverflow(container, id, amount, targetIndex, targetId)
        }
        return revertRemoval(index, id, amount, container, result)
    }

    /**
     * In an overflow scenario as many items are moved as possible with excess being left in their original container
     */
    private fun gracefullyOverflow(container: Container, id: String, amount: Int, targetIndex: Int?, targetId: String): Boolean {
        val index = targetIndex ?: container.indexOf(id)
        val current = container.getAmount(index)

        if (amount == Int.MAX_VALUE && current == Int.MAX_VALUE) {
            return revertRemoval(index, id, amount, container, ContainerResult.Full)
        }

        val overflow = Int.MAX_VALUE - current
        val newAmount = amount - overflow
        val success = if (targetIndex == null) {
            container.add(targetId, overflow, moved = true)
        } else {
            container.add(targetIndex, targetId, overflow, moved = true)
        }

        return revertRemoval(index, id, if (success) newAmount else amount, container, ContainerResult.Full)
    }

    private fun revertRemoval(index: Int?, id: String, amount: Int, container: Container, result: ContainerResult): Boolean {
        val reverted = if (index == null) {
            add(id, amount, moved = true)
        } else {
            add(index, id, amount, moved = true)
        }

        if (!reverted) {
            logger.debug { "Container movement restoration failed $container $id $amount" }
        }

        return result(result)
    }

    private fun track(index: Int, oldItem: Item, item: Item, moved: Boolean) {
        updates.add(ItemChanged(id, index, oldItem, item, moved))
    }

    private fun update() {
        for (events in events) {
            events.emit(ContainerUpdate(container = id, secondary = secondary, updates = updates))
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

        if (stackMode != other.stackMode) return false
        if (!items.contentEquals(other.items)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = stackMode.hashCode()
        result = 31 * result + items.contentHashCode()
        return result
    }

    companion object {
        private val logger = InlineLogger()

        fun setup(
            capacity: Int,
            stackMode: StackMode = StackMode.Normal,
            id: String = "",
            secondary: Boolean = false,
            minimumAmount: Int = 0,
            container: Container = Container(Array(capacity) { Item("", minimumAmount) }),
            events: Events? = null
        ) = container.apply {
            if (!setup) {
                minimumAmounts = IntArray(capacity) { minimumAmount }
                this.id = id
                this.capacity = capacity
                this.stackMode = stackMode
                definitions = get()
                if (events != null) {
                    this.events.add(events)
                }
                this.secondary = secondary
                this.setup = true
            }
        }
    }
}