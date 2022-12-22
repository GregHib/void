package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.entity.character.contain.restrict.ItemRestrictionRule
import world.gregs.voidps.engine.entity.definition.ItemDefinitions

class GeneralStoreRestrictions(
    private val definitions: ItemDefinitions
) : ItemRestrictionRule {

    override fun restricted(id: String): Boolean {
        return id == "coins" || !definitions.get(id)["tradeable", true] || !definitions.contains(id)
    }
}