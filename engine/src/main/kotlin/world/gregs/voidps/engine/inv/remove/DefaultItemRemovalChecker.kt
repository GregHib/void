package world.gregs.voidps.engine.inv.remove

object DefaultItemRemovalChecker : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return 0
    }
}