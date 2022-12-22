package world.gregs.voidps.engine.entity.item

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.utility.get

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class Item(
    val id: String = "",
    val amount: Int = 0,
    @get:JsonIgnore
    val def: ItemDefinition = get<ItemDefinitions>().get(id)
) {

    @JsonIgnore
    fun isEmpty() = id.isBlank()

    @JsonIgnore
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