package world.gregs.voidps.engine.inv.restrict

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.item.Item

class ValidItemRestriction(
    private val definitions: ItemDefinitions,
) : ItemRestrictionRule {
    override fun restricted(id: String): Boolean = id.isBlank() || !definitions.contains(id)

    override fun replacement(id: String): Item? {
        val definition = definitions.getOrNull(id) ?: return null
        val replacement: String = definition.getOrNull("degrade") ?: return null
        if (replacement == "destroy") {
            return Item.EMPTY
        }
        val replaceDefinition = definitions.get(replacement)
        return Item(replacement, replaceDefinition["charges", 1]) // Use charges or default to 1 for amount
    }
}
