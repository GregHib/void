package world.gregs.voidps.engine.inv.remove

class ItemIndexRemovalChecker(
    private val minimumQuantities: IntArray,
    private val default: Int
) : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return minimumQuantities.getOrNull(index) ?: default
    }
}