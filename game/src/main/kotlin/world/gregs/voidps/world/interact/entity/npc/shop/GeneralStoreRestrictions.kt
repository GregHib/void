package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions

class GeneralStoreRestrictions(
    private val definitions: ItemDefinitions
) : ItemRestrictionRule {

    override fun restricted(id: String): Boolean {
        return id == "coins" || !definitions.get(id)["tradeable", true] || !definitions.contains(id)
    }
}