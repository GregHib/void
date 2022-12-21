package world.gregs.voidps.engine.entity.character.contain

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.contain.remove.DefaultItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.remove.ItemRemovalChecker
import world.gregs.voidps.engine.entity.character.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.entity.character.contain.restrict.NoRestrictions
import world.gregs.voidps.engine.entity.character.contain.stack.AlwaysStack
import world.gregs.voidps.engine.entity.character.contain.stack.ItemStackingRule
import world.gregs.voidps.engine.entity.character.contain.transact.Transaction
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Events

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

    val indices: IntRange = items.indices

    var capacity: Int = items.size

    val transaction: Transaction by lazy { Transaction(this) }

    fun transaction(block: Transaction.() -> Unit): Boolean {
        transaction.start()
        block.invoke(transaction)
        return transaction.commit()
    }

    fun stackable(id: String) = stackRule.stackable(id)

    fun needsRemoval(amount: Int, index: Int = -1) = removalCheck.shouldRemove(amount, index)

    fun restricted(id: String) = itemRule.restricted(id)

    fun inBounds(index: Int) = index in items.indices

    val count: Int
        get() = items.indices.count { !isIndexFree(it) }

    val spaces: Int
        get() = items.indices.count { isIndexFree(it) }

    fun isEmpty() = items.indices.all { isIndexFree(it) }

    fun isFull() = items.indices.none { isIndexFree(it) }

    fun isNotFull() = items.indices.any { isIndexFree(it) }

    fun getItemId(index: Int): String = items.getOrNull(index)?.id ?: ""

    fun getItem(index: Int): Item = items.getOrNull(index) ?: Item("", removalCheck.getMinimum(index))

    fun getAmount(index: Int): Int = items.getOrNull(index)?.amount ?: 0

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

    fun isValid(index: Int, id: String, amount: Int) = isValidId(index, id) && isValidAmount(index, amount)

    fun isValidId(index: Int, id: String) = inBounds(index) && items[index].id == id

    fun isValidAmount(index: Int, amount: Int) = inBounds(index) && items[index].amount == amount

    fun isValidInput(id: String, amount: Int): Boolean {
        return !restricted(id) && !needsRemoval(amount)
    }


    /**
     * Checks if an index is free
     */
    fun isIndexFree(index: Int) = needsRemoval(items[index].amount, index)

    fun freeIndex(): Int {
        for (index in items.indices) {
            if (isIndexFree(index)) {
                return index
            }
        }
        return -1
    }

    fun getCount(id: String): Int = getCountLong(id).toInt()

    fun getCountLong(id: String): Long {
        if (id.isBlank()) {
            return 0
        }
        return items.sumOf { if (it.isNotEmpty() && it.id == id) it.amount.toLong() else 0L }
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