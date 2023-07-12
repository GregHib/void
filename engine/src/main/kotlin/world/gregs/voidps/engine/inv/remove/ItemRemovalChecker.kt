package world.gregs.voidps.engine.inv.remove

interface ItemRemovalChecker {
    fun getMinimum(index: Int = -1): Int

    /**
     * Checks if the minimum [amount] has been reached for the item at the specified [index].
     *
     * @param index The index of the item to check.
     * @param amount The minimum number of items required.
     * @return true if the minimum amount has been reached, false otherwise.
     */
    fun shouldRemove(amount: Int, index: Int = -1): Boolean {
        return amount <= getMinimum(index)
    }
}