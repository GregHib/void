package world.gregs.voidps.engine.entity.item

import org.koin.mp.KoinPlatformTools
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions

class Item(
    val id: String = "",
    amount: Int = 0
) {
    private val value = amount
    val def: ItemDefinition
        get() = defOrNull ?: ItemDefinition.EMPTY
    private val defOrNull: ItemDefinition?
        get() = KoinPlatformTools.defaultContext().getOrNull()?.get<ItemDefinitions>()?.getOrNull(id)
    val amount: Int
        get() = if (defOrNull?.contains("charges") == true) 1 else value
    val charges: Int
        get() = if (defOrNull?.contains("charges") == true) value else 0

    fun isEmpty() = id.isBlank()

    fun isNotEmpty() = id.isNotBlank()

    fun copy(id: String = this.id, amount: Int = this.value) = Item(id, amount)

    override fun toString(): String {
        return "Item(id='$id', amount=$amount, charges=$charges)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Item

        if (id != other.id) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + value
        return result
    }

    companion object {
        val EMPTY = Item("", 0)
    }
}