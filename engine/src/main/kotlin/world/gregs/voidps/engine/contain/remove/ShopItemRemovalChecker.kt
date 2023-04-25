package world.gregs.voidps.engine.contain.remove

object ShopItemRemovalChecker : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return -1
    }
}