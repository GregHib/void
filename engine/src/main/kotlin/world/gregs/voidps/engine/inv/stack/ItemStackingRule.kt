package world.gregs.voidps.engine.inv.stack

/**
 * Rules for whether items can be stacked in an inventory
 */
interface ItemStackingRule {
    /**
     * @returns true if the item with the given id can be stacked, false otherwise
     */
    fun stackable(id: String): Boolean
}
