package world.gregs.voidps.engine.entity.character.contain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.event.Events
import java.util.*

data class Container(
    private val items: Array<String>,
    private val amounts: IntArray
) {

    @JsonIgnore
    var id: Int = -1

    @JsonIgnore
    lateinit var name: String

    @JsonIgnore
    var capacity: Int = items.size

    @JsonIgnore
    var stackMode: StackMode = StackMode.Normal

    @JsonIgnore
    var minimumStack: Int = 0

    @JsonIgnore
    lateinit var definitions: ItemDefinitions

    @JsonIgnore
    lateinit var events: Events

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
        get() = amounts.count { !isFree(it) }

    @get:JsonIgnore
    val spaces: Int
        get() = amounts.count { isFree(it) }

    @JsonIgnore
    fun isEmpty() = amounts.all { isFree(it) }

    @JsonIgnore
    fun isFull() = amounts.none { isFree(it) }

    @JsonIgnore
    fun isNotFull() = amounts.any { isFree(it) }

    fun getItem(index: Int): String = items.getOrNull(index) ?: ""

    fun getItems(): Array<String> = items.clone()

    fun getAmount(index: Int): Int = amounts.getOrNull(index) ?: minimumStack

    fun getAmounts(): IntArray = amounts.clone()

    fun indexOf(id: String) = if (id.isBlank()) -1 else items.indexOf(id)

    fun contains(id: String) = indexOf(id) != -1

    fun inBounds(index: Int) = index in items.indices

    fun isValid(index: Int, id: String, amount: Int) = isValidId(index, id) && isValidAmount(index, amount)

    fun isValidId(index: Int, id: String) = inBounds(index) && items[index] == id

    fun isValidAmount(index: Int, amount: Int) = inBounds(index) && amounts[index] == amount

    fun isValidInput(id: String, amount: Int): Boolean {
        return isValidId(id) && isValidAmount(amount) && definitions.getId(id) != -1 && (predicate == null || predicate!!.invoke(id, amount))
    }

    fun isValidOrEmpty(id: String, amount: Int) = (!isValidId(id) && !isValidAmount(amount)) || isValidInput(id, amount)

    private fun isValidId(id: String) = id.isNotBlank()

    private fun isValidAmount(amount: Int) = amount > minimumStack

    /**
     * Checks [amount] for a slot is empty
     */
    fun isFree(amount: Int) = amount == minimumStack

    /**
     * If values is underflowing [minimumStack]
     */
    fun isUnderMin(amount: Int) = amount < minimumStack

    /**
     * Checks if an index is free
     */
    fun isIndexFree(index: Int) = isFree(amounts[index])

    fun freeIndex(): Int {
        for (index in items.indices) {
            if (isIndexFree(index)) {
                return index
            }
        }
        return -1
    }

    fun getCount(id: String): Long {
        var count = 0L
        if (id.isBlank()) {
            return count
        }
        for (index in items.indices) {
            if (getItem(index) == id && getAmount(index) > minimumStack) {
                count += getAmount(index)
            }
        }
        return count
    }

    /**
     * Clears item at the given index
     * @return successful
     */
    fun clear(index: Int, update: Boolean = true, moved: Boolean = false): Boolean = set(index, "", minimumStack, update, moved)

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
        if (!inBounds(index)) {
            return false
        }
        track(index, items[index], amounts[index], id, amount, moved)
        items[index] = id
        amounts[index] = amount
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
        val tempId = items[firstIndex]
        val tempAmount = amounts[firstIndex]
        set(firstIndex, items[secondIndex], amounts[secondIndex], update = false, moved = true)
        set(secondIndex, tempId, tempAmount, update = false, moved = true)
        update()
        return true
    }

    /**
     * Swaps two indices in different containers
     * @return Whether the indices were swapped
     */
    fun swap(firstIndex: Int, container: Container, secondIndex: Int): Boolean {
        if (!inBounds(firstIndex) || !inBounds(secondIndex)) {
            result(ContainerResult.Invalid)
            container.result(ContainerResult.Invalid)
            return false
        }
        val fromId = items[firstIndex]
        val fromAmount = amounts[firstIndex]
        val toId = container.items[secondIndex]
        val toAmount = container.amounts[secondIndex]

        if (!isValidOrEmpty(toId, toAmount) || !container.isValidOrEmpty(fromId, fromAmount)) {
            result(ContainerResult.Invalid)
            container.result(ContainerResult.Invalid)
            return false
        }
        set(firstIndex, toId, toAmount, moved = true)
        container.set(secondIndex, fromId, fromAmount, moved = true)
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
     * Inserts between items at a specific index
     * @param index The index to insert at
     * @param id The item to add
     * @param amount The stack amount or individual count
     *  @param moved If this action is part of a larger movement transaction
     * @return Whether an item was successfully inserted
     */
    fun insert(index: Int, id: String, amount: Int = 1, moved: Boolean = false): Boolean {
        if (!inBounds(index) || !isValidInput(id, amount)) {
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
            set(i, items[i - 1], amounts[i - 1], update = false)
        }
        set(index, id, amount, moved = moved)
        return result(ContainerResult.Success)
    }

    /**
     * Adds items at a specific index
     * Note: Will never add items outside of the given [index]
     * @param id The item to add
     * @param amount The stack amount or individual count
     *  @param moved If this action is part of a larger movement transaction
     * @return Whether an item was successfully added
     */
    fun add(index: Int, id: String, amount: Int = 1, moved: Boolean = false): Boolean {
        if (!inBounds(index) || !isValidInput(id, amount)) {
            return result(ContainerResult.Invalid)
        }

        val item = items[index]
        if (item.isNotBlank() && item != id) {
            return result(ContainerResult.WrongType)
        }

        val stack = amounts[index]
        val combined = stack + amount

        if (combined > 1 && !stackable(id)) {
            return add(id, amount)
        }

        if (stack xor combined and (amount xor combined) < 0) {
            return result(ContainerResult.Overflow)
        }

        set(index, id, combined, moved = moved)
        return result(ContainerResult.Success)
    }

    /**
     * Adds any number of items stacked or otherwise
     * @param id The id of the item(s) to add
     * @param amount The stack amount or individual count
     *  @param moved If this action is part of a movement transaction
     * @return Whether an item was successfully added
     */
    fun add(id: String, amount: Int = 1, moved: Boolean = false): Boolean {
        if (!isValidInput(id, amount)) {
            return result(ContainerResult.Invalid)
        }
        if (stackable(id)) {
            var index = indexOf(id)
            if (index != -1) {
                val stack = amounts[index]
                val combined = stack + amount

                if (stack xor combined and (amount xor combined) < 0) {
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
        if (!inBounds(index) || !isValidInput(id, amount)) {
            return result(ContainerResult.Invalid)
        }

        val item = items[index]
        if (item != id) {
            return result(ContainerResult.WrongType)
        }

        if (amount > 1 && !stackable(id)) {
            return remove(id, amount, moved)
        }

        checkCombined(index, amount)?.let {
            return result(it)
        }
        val combined = amounts[index] - amount

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
            val combined = amounts[index] - amount
            set(index, id, combined, moved = moved)
        } else {
            val count = items.count { it == id }
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
        val stack = amounts[index]
        val combined = stack - amount

        if (stack xor amount and (stack xor combined) < 0) {
            return ContainerResult.Deficient
        }

        if (isUnderMin(combined)) {
            return ContainerResult.Deficient
        }

        if (isFree(combined)) {
            clear(index)
            return ContainerResult.Success
        }
        return null
    }

    fun sort() {
        val items = LinkedList<String>()
        val amounts = LinkedList<Int>()
        for (i in this.items.indices.reversed()) {
            val id = this.items[i]
            val amount = this.amounts[i]
            if (isFree(amount)) {
                items.addLast(id)
                amounts.addLast(amount)
            } else {
                items.addFirst(id)
                amounts.addFirst(amount)
            }
        }
        items.forEachIndexed { index, id ->
            val amount = amounts[index]
            this.items[index] = id
            this.amounts[index] = amount
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
        val id = getItem(index)
        val amount = getAmount(index)
        if (id.isBlank() || amount == minimumStack) {
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

    private fun track(index: Int, oldItem: String, oldAmount: Int, item: String, amount: Int, moved: Boolean) {
        updates.add(ItemChanged(name, index, oldItem, oldAmount, item, amount, moved))
    }

    private fun update() {
        events.emit(ContainerUpdate(containerId = id, secondary = secondary, updates = updates))
        for (update in updates) {
            events.emit(update)
        }
        updates = mutableListOf()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Container

        if (stackMode != other.stackMode) return false
        if (!items.contentEquals(other.items)) return false
        if (!amounts.contentEquals(other.amounts)) return false
        if (minimumStack != other.minimumStack) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stackMode.hashCode()
        result = 31 * result + items.contentHashCode()
        result = 31 * result + amounts.contentHashCode()
        result = 31 * result + minimumStack
        return result
    }

    companion object {
        private val logger = InlineLogger()
    }
}