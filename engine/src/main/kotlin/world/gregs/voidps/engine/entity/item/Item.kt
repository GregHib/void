package world.gregs.voidps.engine.entity.item

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.utility.get

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class Item(
    val name: String = "",
    val amount: Int = 0,
    val charge: Int = 0
) {
    @get:JsonIgnore
    val def: ItemDefinition
        get() = get<ItemDefinitions>().get(name)
    @get:JsonIgnore
    val id: Int
        get() = def.id

    @JsonIgnore
    fun isEmpty() = name.isBlank()

    @JsonIgnore
    fun isNotEmpty() = name.isNotBlank()

    fun toNote(): Item? = if (def.notedTemplateId != -1 && def.noteId != -1) {
        copy(name = get<ItemDefinitions>().getName(def.noteId))
    } else {
        null
    }

    companion object {
        val EMPTY = Item("", 0, 0)
    }
}