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

class Container(
    internal val data: ContainerData,
    val id: String = "",
    var itemRule: ItemRestrictionRule = NoRestrictions,
    private val stackRule: ItemStackingRule = AlwaysStack,
    private val removalCheck: ItemRemovalChecker = DefaultItemRemovalChecker
) {

    val items: Array<Item>
        get() = data.items
    val indices: IntRange = items.indices
    val size: Int = items.size

    val count: Int
        get() = size - spaces

    val spaces: Int
        get() = items.count { it.isEmpty() }

    val transaction = Transaction(this)

    fun isEmpty() = count == 0

    fun isFull() = spaces == 0

    fun inBounds(index: Int) = index in items.indices

    operator fun get(index: Int): Item = getOrNull(index) ?: Item("", removalCheck.getMinimum(index))

    fun getOrNull(index: Int) = items.getOrNull(index)

    fun indexOf(id: String) = if (id.isBlank()) -1 else items.indexOfFirst { it.id == id }

    fun freeIndex(): Int = items.indexOfFirst { it.isEmpty() }

    fun count(id: String): Int {
        if (id.isBlank()) {
            return 0
        }
        return items
            .sumOf { if (it.id == id && it.amount > 0) it.amount.toLong() else 0L }
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }

    fun contains(id: String) = indexOf(id) != -1

    fun contains(id: String, amount: Int): Boolean {
        if (!stackable(id)) {
            return count(id) >= amount
        }
        val index = indexOf(id)
        if (index == -1) {
            return false
        }
        return get(index).amount >= amount
    }

    fun stackable(id: String) = stackRule.stackable(id)

    fun shouldRemove(amount: Int, index: Int = -1) = removalCheck.shouldRemove(amount, index)

    fun restricted(id: String) = itemRule.restricted(id)

    fun transaction(block: Transaction.() -> Unit): Boolean {
        transaction.start()
        block.invoke(transaction)
        return transaction.commit()
    }

    override fun toString(): String {
        return "Container($id)"
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