package world.gregs.voidps.engine.contain.remove

object DefaultItemRemovalChecker : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return 0
    }
}