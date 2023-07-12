package world.gregs.voidps.engine.inv.remove

class ItemIndexRemovalChecker(
    private val minimumQuantities: IntArray
) : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return minimumQuantities[index]
    }
}