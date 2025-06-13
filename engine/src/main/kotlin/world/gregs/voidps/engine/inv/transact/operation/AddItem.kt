package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.transact.TransactionError

/**
 * Transaction operation for adding items to an inventory.
 * Stackable items are added to existing stack, or it is added to the first empty slot if no matching stack is found.
 * Items that aren't stackable are added to one or more empty slots, depending on amount and whether it is one or greater.
 */
object AddItem {

    /**
     * Adds an item to the inventory.
     * @param id the identifier of the item to be added.
     * @param amount the number of items to be added. Default value is 1.
     */
    fun TransactionOperation.add(id: String, amount: Int = 1) {
        if (failed) {
            return
        }
        if (inventory.restricted(id) || amount <= inventory.amountBounds.minimum()) {
            error = TransactionError.Invalid
            return
        }
        // Check if the item is stackable
        if (!inventory.stackable(id)) {
            addItemsToSlots(id, amount)
            return
        }
        // Try to add the item to an existing stack
        val index = inventory.indexOf(id)
        if (index != -1) {
            increaseStack(index, amount)
            return
        }
        // Add new item stack
        addItemToEmptySlot(id, amount)
    }

    /**
     * Increases the stack at the specified index by the given amount.
     * @param index the index of the stack to be increased.
     * @param amount the number of items to be added to the stack.
     */
    fun TransactionOperation.increaseStack(index: Int, amount: Int) {
        val item = inventory[index]
        if (item.isEmpty()) {
            error = TransactionError.Invalid
            return
        }
        // Check if the stack would exceed the maximum integer value
        if (item.amount + amount.toLong() > Int.MAX_VALUE) {
            error = TransactionError.Full(Int.MAX_VALUE - item.amount)
            return
        }
        // Combine the stacks and update the item in the inventory
        set(index, item.copy(amount = item.amount + amount))
    }

    /**
     * Adds the item to an empty slot in the inventory.
     * @param id the identifier of the item to be added.
     * @param amount the number of items to be added to the stack.
     **/
    private fun TransactionOperation.addItemToEmptySlot(id: String, amount: Int) {
        // Find an empty slot in the inventory
        val emptySlot = inventory.freeIndex()
        if (emptySlot != -1) {
            // Add the item to the empty slot.
            set(emptySlot, Item(id, amount))
            return
        }
        // No empty slot was found
        error = TransactionError.Full()
    }

    /**
     * Adds the items to one or more empty slots in the inventory.
     * @param id the identifier of the items to be added.
     * @param amount the number of items to be added.
     */
    private fun TransactionOperation.addItemsToSlots(id: String, amount: Int) {
        for (count in 0 until amount) {
            // Find an empty slot in the inventory
            val emptySlot = inventory.freeIndex()
            if (emptySlot == -1) {
                error = TransactionError.Full(count)
                return
            }
            // Add one item to the empty slot
            set(emptySlot, Item(id, 1))
        }
    }
}
