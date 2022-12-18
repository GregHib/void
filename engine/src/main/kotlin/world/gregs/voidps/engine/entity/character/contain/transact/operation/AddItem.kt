package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

/**
 * Transaction operation for adding items to a container.
 * Stackable items are added to existing stack, or it is added to the first empty slot if no matching stack is found.
 * Items that aren't stackable are added to one or more empty slots, depending on quantity and whether it is one or greater.
 */
interface AddItem : TransactionOperation {

    /**
     * Adds an item to the container.
     * @param id the identifier of the item to be added.
     * @param quantity the number of items to be added. Default value is 1.
     */
    fun add(id: String, quantity: Int = 1) {
        // Return if the transaction has failed.
        if (failed) {
            return
        }
        if (invalid(id, quantity)) {
            error(TransactionError.Invalid)
            return
        }
        // Check if the item is stackable
        if (stackable(id)) {
            // Try to add the item to an existing stack
            val index = indexOfFirst { it?.id == id }
            if (index != -1) {
                return increaseStack(index, quantity)
            }
            return addItemToEmptySlot(id, quantity)
        } else {
            return addItemsToSlots(id, quantity)
        }
    }

    /**
     * Increases the stack at the specified index by the given quantity.
     * @param index the index of the stack to be increased.
     * @param quantity the number of items to be added to the stack.
     */
    private fun increaseStack(index: Int, quantity: Int) {
        val stack = get(index)
        // check if the stack would exceed the maximum integer value
        if (stack == null) {
            error(TransactionError.Invalid)
            return
        }
        if (stack.amount + quantity.toLong() > Int.MAX_VALUE) {
            error(TransactionError.Overflow(Int.MAX_VALUE - stack.amount))
            return
        }
        // Combine the stacks and update the item in the container
        set(index, stack.copy(amount = stack.amount + quantity))
    }

    /**
     * Adds the item to an empty slot in the container.
     * @param id the identifier of the item to be added.
     * @param quantity the number of items to be added to the stack.
     **/
    private fun addItemToEmptySlot(id: String, quantity: Int) {
        // Find an empty slot in the container
        val emptySlot = emptyIndex()
        if (emptySlot != -1) {
            // Add the item to the empty slot.
            set(emptySlot, Item(id, quantity))
            return
        }
        // No empty slot was found
        error(TransactionError.Full(0))
    }


    /**
     * Adds the items to one or more empty slots in the container.
     * @param id the identifier of the items to be added.
     * @param quantity the number of items to be added.
     */
    private fun addItemsToSlots(id: String, quantity: Int) {
        repeat(quantity) { count ->
            // Find an empty slot in the container
            val emptySlot = emptyIndex()
            if (emptySlot == -1) {
                error(TransactionError.Full(count))
                return
            }
            // add one item to the empty slot
            set(emptySlot, Item(id, 1))
        }
    }

}