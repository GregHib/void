package rs.dusk.engine.entity.character.contain

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.definition.decoder.ItemDecoder
import java.util.*

data class Container(
    private val decoder: ItemDecoder,
    val listeners: MutableList<(List<Triple<Int, Int, Int>>) -> Unit> = mutableListOf(),
    val stackMode: StackMode = StackMode.Normal,
    val items: IntArray,
    val amounts: IntArray,
    val minimumStack: Int = 0
) {

    constructor(
        decoder: ItemDecoder,
        capacity: Int,
        stackMode: StackMode = StackMode.Normal,
        listeners: MutableList<(List<Triple<Int, Int, Int>>) -> Unit> = mutableListOf(),
        minimumStack: Int = 0
    ) : this(
        decoder,
        listeners,
        stackMode,
        IntArray(capacity) { -1 },
        IntArray(capacity) { minimumStack },
        minimumStack
    )

    private var updates = mutableListOf<Triple<Int, Int, Int>>()
    private val logger = InlineLogger()

    fun stackable(id: Int) = when (stackMode) {
        StackMode.Always -> true
        StackMode.Never -> false
        StackMode.Normal -> decoder.getSafe(id).stackable == 1
    }

    val spaces: Int
        get() = amounts.count { isFree(it) }

    fun indexOf(id: Int) = items.indexOf(id)

    fun inBounds(index: Int) = index in items.indices

    fun isValid(index: Int, id: Int, amount: Int) = isValidId(index, id) && isValidAmount(index, amount)

    fun isValidId(index: Int, id: Int) = inBounds(index) && items[index] == id

    fun isValidAmount(index: Int, amount: Int) = inBounds(index) && amounts[index] == amount

    fun isValidInput(id: Int, amount: Int): Boolean {
        return id >= 0 && amount > 0 && id < decoder.size
    }

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

    /**
     * Clears item at the given index
     * @return successful
     */
    fun clear(index: Int, update: Boolean = true): Boolean = set(index, -1, minimumStack, update)

    /**
     * Clears all indices
     */
    fun clearAll() {
        items.fill(-1)
        amounts.fill(minimumStack)
    }

    fun set(index: Int, id: Int, amount: Int = 1, update: Boolean = true): Boolean {
        if (!inBounds(index)) {
            return false
        }
        items[index] = id
        amounts[index] = amount
        track(index, id, amount)
        if (update) {
            update()
        }
        return true
    }

    /**
     * Switches two indices
     * @return Whether the indices were switched
     */
    fun switch(firstIndex: Int, secondIndex: Int): Boolean {
        if (!inBounds(firstIndex) || !inBounds(secondIndex)) {
            return false
        }
        val tempId = items[firstIndex]
        val tempAmount = amounts[firstIndex]
        set(firstIndex, items[secondIndex], amounts[secondIndex], false)
        set(secondIndex, tempId, tempAmount, false)
        update()
        return true
    }

    /**
     * Switches two indices in different containers
     * @return Whether the indices were switched
     */
    fun switch(firstIndex: Int, container: Container, secondIndex: Int): Boolean {
        if (!inBounds(firstIndex) || !inBounds(secondIndex)) {
            return false
        }
        val tempId = items[firstIndex]
        val tempAmount = amounts[firstIndex]
        set(firstIndex, container.items[secondIndex], container.amounts[secondIndex])
        container.set(secondIndex, tempId, tempAmount)
        return true
    }

    /**
     * Replaces one unstackable item with another
     * @param id The item id to replace
     * @param replacement The replacement item id
     * @return Whether the item was found and replaced successfully
     */
    fun replace(id: Int, replacement: Int): Boolean {
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
     * Adds items at a specific index
     * Note: Will never add items outside of the given [index]
     * @param id The id of the item(s) to add
     * @param amount The stack amount or individual count
     * @return Whether an item was successfully added
     */
    fun add(index: Int, id: Int, amount: Int = 1): ContainerResult.Addition {
        if (!inBounds(index) || !isValidInput(id, amount)) {
            return ContainerResult.Addition.Failure.Invalid
        }

        val item = items[index]
        if (item != -1 && item != id) {
            return ContainerResult.Addition.Failure.WrongType
        }

        val stack = amounts[index]
        val combined = stack + amount

        if (stack xor combined and (amount xor combined) < 0) {
            return ContainerResult.Addition.Failure.Overflow
        }

        if (combined > 1 && !stackable(id)) {
            return ContainerResult.Addition.Failure.Unstackable
        }

        set(index, id, combined)
        return ContainerResult.Addition.Added
    }

    /**
     * Adds any number of items stacked or otherwise
     * @param id The id of the item(s) to add
     * @param amount The stack amount or individual count
     * @return Whether an item was successfully added
     */
    fun add(id: Int, amount: Int = 1): ContainerResult.Addition {
        if (!isValidInput(id, amount)) {
            return ContainerResult.Addition.Failure.Invalid
        }
        if (stackable(id)) {
            var index = indexOf(id)
            if (index != -1) {
                val stack = amounts[index]
                val combined = stack + amount

                if (stack xor combined and (amount xor combined) < 0) {
                    return ContainerResult.Addition.Failure.Overflow
                }

                set(index, id, combined)
            } else {
                index = freeIndex()
                if (index == -1) {
                    return ContainerResult.Addition.Failure.Full
                }

                set(index, id, amount)
            }
        } else {
            if (spaces < amount) {
                return ContainerResult.Addition.Failure.Full
            }

            repeat(amount) {
                val index = freeIndex()
                set(index, id, amount = 1, update = false)
            }
            update()
        }
        return ContainerResult.Addition.Added
    }

    /**
     *  Removes items from a specific container index
     *  Note: Will never remove items not in this index
     *  @param id The item id to remove
     *  @param amount The stack number to remove (default 1 for unstackable)
     *  @return Whether an item was successfully removed
     */
    fun remove(index: Int, id: Int, amount: Int = 1): ContainerResult.Removal {
        if (!inBounds(index) || !isValidInput(id, amount)) {
            return ContainerResult.Removal.Failure.Invalid
        }

        val item = items[index]
        if (item != id) {
            return ContainerResult.Removal.Failure.WrongType
        }

        val stack = amounts[index]
        val combined = stack - amount

        if (stack xor amount and (stack xor combined) < 0) {
            return ContainerResult.Removal.Failure.Underflow
        }

        if (isUnderMin(combined)) {
            return ContainerResult.Removal.Failure.Underflow
        }

        if (isFree(combined)) {
            clear(index)
            return ContainerResult.Removal.Removed
        }

        if (combined > 1 && !stackable(id)) {
            return ContainerResult.Removal.Failure.Unstackable
        }

        set(index, id, combined)
        return ContainerResult.Removal.Removed
    }

    /**
     *  Removes any number of items stacked or otherwise
     *  @param id The item id to remove
     *  @param amount The stack or individual number of items to remove
     *  @return Whether an item was successfully removed
     */
    fun remove(id: Int, amount: Int = 1): ContainerResult.Removal {
        if (!isValidInput(id, amount)) {
            return ContainerResult.Removal.Failure.Invalid
        }
        var index = indexOf(id)
        if (index == -1) {
            return ContainerResult.Removal.Failure.Deficient
        }

        if (stackable(id)) {
            val stack = amounts[index]
            val combined = stack - amount

            if (stack xor amount and (stack xor combined) < 0) {
                return ContainerResult.Removal.Failure.Underflow
            }

            if (isUnderMin(combined)) {
                return ContainerResult.Removal.Failure.Underflow
            }

            if (isFree(combined)) {
                clear(index)
                return ContainerResult.Removal.Removed
            }

            set(index, id, combined)
        } else {
            val count = items.count { it == id }
            if (count < amount) {
                return ContainerResult.Removal.Failure.Deficient
            }

            repeat(amount) {
                index = indexOf(id)
                clear(index, update = false)
            }
            update()
        }
        return ContainerResult.Removal.Removed
    }

    fun sort() {
        val items = LinkedList<Int>()
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
            this.items[index] = id
        }
        amounts.forEachIndexed { index, id ->
            this.amounts[index] = id
        }
    }

    fun move(
        container: Container,
        id: Int,
        amount: Int = 1,
        index: Int? = null,
        targetIndex: Int? = null
    ): ContainerResult {
        var result: ContainerResult = if (index == null) {
            remove(id, amount)
        } else {
            remove(index, id, amount)
        }

        if (result !is ContainerResult.Removal.Removed) {
            return result
        }

        result = if (targetIndex == null) {
            container.add(id, amount)
        } else {
            container.add(targetIndex, id, amount)
        }

        if (result is ContainerResult.Addition.Added) {
            return result
        }

        // Undo removal when addition fails
        val revertResult = if (index == null) {
            add(id, amount)
        } else {
            add(index, id, amount)
        }

        if (revertResult !is ContainerResult.Addition.Added) {
            logger.debug { "Container movement restoration failed $container $id $amount" }
        }

        return result
    }

    private fun track(index: Int, id: Int, amount: Int) {
        updates.add(Triple(index, id, amount))
    }

    private fun update() {
        listeners.forEach {
            it.invoke(updates)
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
}