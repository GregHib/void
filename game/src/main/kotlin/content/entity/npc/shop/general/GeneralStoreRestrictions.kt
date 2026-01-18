package content.entity.npc.shop.general

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule

object GeneralStoreRestrictions : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        if (id == "coins") {
            return false
        }
        val def = ItemDefinitions.getOrNull(id) ?: return false
        return !def["tradeable", true] || def.lendTemplateId != -1 || def.dummyItem != 0
    }
}
