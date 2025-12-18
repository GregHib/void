package world.gregs.voidps.cache.type.list

import world.gregs.voidps.cache.type.TypeList
import world.gregs.voidps.cache.type.types.ItemType

class ItemTypeList(
    override val types: Array<ItemType?>
) : TypeList<ItemType> {
    override fun empty() = ItemType.EMPTY
}