package world.gregs.voidps.engine.contain.stack

import world.gregs.voidps.engine.data.definition.extra.ItemDefinitions

/**
 * Rules for whether items can be stacked in a container
 */
interface ItemStackingRule {
    /**
     * @returns true if the item with the given id can be stacked, false otherwise
     */
    fun stackable(id: String): Boolean
}

/**
 * Forces all items, even un-stackable items, to be stacked
 */
object AlwaysStack : ItemStackingRule {
    override fun stackable(id: String) = true
}

/**
 * Forces all items, even stackable items, to be unstacked
 */
object NeverStack : ItemStackingRule {
    override fun stackable(id: String) = false
}

/**
 * Checks individual items are stackable
 */
class DependentOnItem(
    private val definitions: ItemDefinitions
) : ItemStackingRule {
    override fun stackable(id: String) = definitions.get(id).stackable == 1
}