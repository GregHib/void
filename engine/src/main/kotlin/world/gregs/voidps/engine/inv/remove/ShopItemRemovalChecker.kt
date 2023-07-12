package world.gregs.voidps.engine.inv.remove

object ShopItemRemovalChecker : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return -1
    }
}