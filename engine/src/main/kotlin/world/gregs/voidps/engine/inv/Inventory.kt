package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.remove.DefaultItemAmountBounds
import world.gregs.voidps.engine.inv.remove.ItemAmountBounds
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.inv.restrict.NoRestrictions
import world.gregs.voidps.engine.inv.stack.AlwaysStack
import world.gregs.voidps.engine.inv.stack.ItemStackingRule
import world.gregs.voidps.engine.inv.transact.Transaction

class Inventory(
    internal var data: Array<Item>,
    val id: String = "",
    var itemRule: ItemRestrictionRule = NoRestrictions,
    private val stackRule: ItemStackingRule = AlwaysStack,
    internal val amountBounds: ItemAmountBounds = DefaultItemAmountBounds,
) {

    val items: Array<Item>
        get() = data
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

    operator fun get(index: Int): Item = getOrNull(index) ?: Item("", amountBounds.minimum(index))

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

    /**
     * Count how many multiples of [amount] this inventory has
     */
    fun count(id: String, amount: Int): Int {
        if (id.isBlank() || amount == 0) {
            return 0
        }
        return count(id) / amount
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

    fun restricted(id: String) = itemRule.restricted(id)

    fun transaction(block: Transaction.() -> Unit): Boolean {
        transaction.start()
        block.invoke(transaction)
        return transaction.commit()
    }

    override fun toString(): String = "Inventory($id)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Inventory

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
            amountBounds: ItemAmountBounds = DefaultItemAmountBounds,
        ) = Inventory(
            Array(capacity) { Item("", amountBounds.minimum(it)) },
            id,
            itemRule,
            stackRule,
            amountBounds,
        )
    }
}
