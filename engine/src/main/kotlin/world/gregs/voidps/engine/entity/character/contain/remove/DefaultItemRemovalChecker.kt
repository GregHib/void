package world.gregs.voidps.engine.entity.character.contain.remove

object DefaultItemRemovalChecker : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return 0
    }
}