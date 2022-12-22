package world.gregs.voidps.engine.entity.character.contain.remove

object ShopItemRemovalChecker : ItemRemovalChecker {
    override fun getMinimum(index: Int): Int {
        return -1
    }
}