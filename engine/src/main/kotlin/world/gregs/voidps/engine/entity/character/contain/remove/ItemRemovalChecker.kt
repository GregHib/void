package world.gregs.voidps.engine.entity.character.contain.remove

interface ItemRemovalChecker {
    fun getMinimum(index: Int = -1): Int

    fun isValid(quantity: Int, index: Int = -1): Boolean = quantity > getMinimum(index)

    /**
     * Checks if the minimum [quantity] has been reached for the item at the specified [index].
     *
     * @param index The index of the item to check.
     * @param quantity The minimum quantity required.
     * @return true if the minimum quantity has been reached, false otherwise.
     */
    fun shouldRemove(index: Int, quantity: Int): Boolean {
        return quantity == getMinimum(index)
    }
}