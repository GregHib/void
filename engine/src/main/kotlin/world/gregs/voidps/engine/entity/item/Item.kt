package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.utility.get

data class Item(
    val name: String,
    val amount: Int = 1,
    val charge: Int = 0
) {
    val def: ItemDefinition
        get() = get<ItemDefinitions>().get(name)
    val id: Int
        get() = def.id

    fun isEmpty() = name.isBlank()
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