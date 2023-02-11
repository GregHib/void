package world.gregs.voidps.engine.contain.restrict

import world.gregs.voidps.engine.entity.definition.ItemDefinitions

class ValidItemRestriction(
    private val definitions: ItemDefinitions
) : ItemRestrictionRule {
    override fun restricted(id: String): Boolean {
        return id.isBlank() || !definitions.contains(id)
    }
}