package world.gregs.voidps.engine.inv.stack

/**
 * Forces all items, even stackable items, to be unstacked
 */
object NeverStack : ItemStackingRule {
    override fun stackable(id: String) = false
}
