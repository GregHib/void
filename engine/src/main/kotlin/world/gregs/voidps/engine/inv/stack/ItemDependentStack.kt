package world.gregs.voidps.engine.inv.stack

import world.gregs.voidps.engine.data.definition.ItemDefinitions

/**
 * Checks individual items are stackable
 */
object ItemDependentStack : ItemStackingRule {
    override fun stackable(id: String) = ItemDefinitions.get(id).stackable == 1
}
