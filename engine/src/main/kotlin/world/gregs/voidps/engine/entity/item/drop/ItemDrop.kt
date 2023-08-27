package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get

data class ItemDrop(
    val id: String,
    val amount: IntRange,
    val chance: Int = 1
) : Drop {

    init {
        assert(chance > 0) { "Item must have a positive chance." }
    }

    fun toItem(definitions: ItemDefinitions = get()): Item {
        if (id == "nothing" || id.isBlank()) {
            return Item.EMPTY
        }
        return Item(id, amount.random(), definitions.get(id))
    }
}