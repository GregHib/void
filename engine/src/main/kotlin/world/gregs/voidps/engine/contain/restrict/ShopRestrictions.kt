package world.gregs.voidps.engine.contain.restrict

import world.gregs.voidps.engine.entity.item.Item

class ShopRestrictions(
    private val items: Array<Item>
) : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        return items.indexOfFirst { it.id == id } == -1
    }
}