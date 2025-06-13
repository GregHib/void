package world.gregs.voidps.engine.inv.restrict

import world.gregs.voidps.engine.entity.item.Item

class ShopRestrictions(
    private val items: Array<Item>,
) : ItemRestrictionRule {
    override fun restricted(id: String): Boolean = items.indexOfFirst { it.id == id } == -1
}
