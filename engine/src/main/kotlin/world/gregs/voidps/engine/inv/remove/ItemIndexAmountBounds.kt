package world.gregs.voidps.engine.inv.remove

class ItemIndexAmountBounds(
    private val minimumQuantities: IntArray,
    private val default: Int
) : ItemAmountBounds {
    override fun minimum(index: Int): Int {
        return minimumQuantities.getOrNull(index) ?: default
    }
}