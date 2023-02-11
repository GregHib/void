package world.gregs.voidps.engine.contain.remove

class ItemIndexRemovalChecker(
    private val minimumQuantities: IntArray
) : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return minimumQuantities[index]
    }
}