package world.gregs.voidps.engine.inv.restrict

import world.gregs.voidps.engine.data.definition.ItemDefinitions

class ValidItemRestriction(
    private val definitions: ItemDefinitions
) : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        return id.isBlank() || !definitions.contains(id)
    }
}