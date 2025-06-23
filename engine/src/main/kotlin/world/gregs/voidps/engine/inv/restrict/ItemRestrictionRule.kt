package world.gregs.voidps.engine.inv.restrict

import world.gregs.voidps.engine.entity.item.Item

interface ItemRestrictionRule {
    fun restricted(id: String): Boolean
    fun replacement(id: String): Item? = null
}
