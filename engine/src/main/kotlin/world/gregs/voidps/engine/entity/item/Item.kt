package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.get

data class Item(
    val id: String = "",
    val amount: Int = 0,
    val def: ItemDefinition = get<ItemDefinitions>().get(id)
) {

    fun isEmpty() = id.isBlank()

    fun isNotEmpty() = id.isNotBlank()

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
        val EMPTY = Item("", 0, ItemDefinition.EMPTY)
    }
}