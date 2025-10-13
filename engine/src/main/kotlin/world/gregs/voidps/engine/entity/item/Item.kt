package world.gregs.voidps.engine.entity.item

import org.koin.mp.KoinPlatformTools
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions

class Item(
    val id: String = "",
    amount: Int = 1,
) {
    internal val value = amount
    val def: ItemDefinition
        get() = defOrNull ?: ItemDefinition.EMPTY
    private val defOrNull: ItemDefinition?
        get() = KoinPlatformTools.defaultContext().getOrNull()?.get<ItemDefinitions>()?.getOrNull(id)
    private val itemCharge: Boolean
        get() = defOrNull?.contains("charges") == true && defOrNull?.contains("charge") != true
    val amount: Int
        get() = if (itemCharge) 1 else value

    fun isEmpty() = id.isBlank()

    fun isNotEmpty() = id.isNotBlank()

    fun copy(id: String = this.id, amount: Int = this.value) = Item(id, amount)

    fun rawValue() = value

    override fun toString(): String = "Item(id='$id', amount=$value)"

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
