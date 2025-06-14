package world.gregs.voidps.engine.inv.stack

import world.gregs.voidps.engine.data.definition.ItemDefinitions

/**
 * Checks individual items are stackable
 */
class ItemDependentStack(
    private val definitions: ItemDefinitions,
) : ItemStackingRule {
    override fun stackable(id: String) = definitions.get(id).stackable == 1
}
