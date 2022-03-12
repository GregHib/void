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
    var charge: Int = 0,
    @get:JsonIgnore
    val def: ItemDefinition = get<ItemDefinitions>().get(id)
) {

    @JsonIgnore
    fun isEmpty() = id.isBlank()

    @JsonIgnore
    fun isNotEmpty() = id.isNotBlank()

    companion object {
        val EMPTY = Item("", 0, 0, ItemDefinition.EMPTY)
    }
}