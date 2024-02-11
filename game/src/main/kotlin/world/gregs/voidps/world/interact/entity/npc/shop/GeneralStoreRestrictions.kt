package world.gregs.voidps.world.interact.entity.npc.shop

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.inv.restrict.ItemRestrictionRule

class GeneralStoreRestrictions(
    private val definitions: ItemDefinitions
) : ItemRestrictionRule {

    override fun restricted(id: String): Boolean {
        if (id == "coins") {
            return false
        }
        val def = definitions.getOrNull(id) ?: return false
        return !def["tradeable", true] || def.lendTemplateId != -1 || def.singleNoteTemplateId != -1 || def.dummyItem != 0
    }
}