package world.gregs.voidps.engine.entity.character.contain.remove

interface ItemRemovalChecker {
    fun getMinimum(index: Int = 0): Int

    fun shouldRemove(index: Int, quantity: Int): Boolean {
        return quantity == getMinimum(index)
    }
}