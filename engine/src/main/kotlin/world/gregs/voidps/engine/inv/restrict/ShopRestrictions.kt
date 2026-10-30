package world.gregs.voidps.engine.inv.restrict

import world.gregs.voidps.engine.inv.Inventory

class ShopRestrictions(
    private val inventory: Inventory,
) : ItemRestrictionRule {
    override fun restricted(id: String): Boolean = inventory.items.indexOfFirst { it.id == id } == -1
}
