package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.get

class Item(
    val id: String = "",
    amount: Int = 0
) {
    private val actualAmount = amount
    val def: ItemDefinition
        get() = get<ItemDefinitions>().get(id)
    val amount: Int
        get() = if (def.contains("charges")) 1 else actualAmount
    val charges: Int
        get() = if (def.contains("charges")) actualAmount else 0

    fun isEmpty() = id.isBlank()

    fun isNotEmpty() = id.isNotBlank()

    fun copy(id: String = this.id, amount: Int = this.amount) = Item(id, amount)

    override fun toString(): String {
        return "Item(id='$id', amount=$amount)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (id != other.id) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + amount
        return result
    }

    companion object {
        val EMPTY = Item("", 0)
    }
}