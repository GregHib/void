package world.gregs.voidps.engine.entity.character.contain.transact.operation

import world.gregs.voidps.engine.entity.character.contain.Container
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.item.Item

/**
 * An interface representing operations that can be performed on a [Container] as part of a [Transaction].
 * These operations may be reversible or undoable, and may involve changes to one or more containers.
 *
 * @property error The error that occurred during the transaction, if any
 * @property failed Whether the transaction has failed
 * @property indices The indices of the items in the container
 */
interface TransactionOperation {
    var error: TransactionError?
    val failed: Boolean
        get() = error != null

    /**
     * The indices of the items in the container
     */
    val indices: IntRange

    /**
     * @return the first index where the [block] returns true
     *
     * @param block The block to be applied to each item in the container.
     * @return The index of the first item for which the block returns true, or -1 if no such item is found.
     */
    fun indexOfFirst(block: (Item?) -> Boolean): Int

    /**
     * Finds the first empty index in the container.
     *
     * @return The index of the first empty slot in the container, or -1 if the container is full.
     */
    fun emptyIndex(): Int

    /**
     * Gets the item at the specified index.
     *
     * @param index The index of the item to retrieve.
     * @return The item at the specified index, or null if the slot is empty.
     */
    fun get(index: Int): Item?

    /**
     * Checks if the item with [id] is stackable according to the containers [ItemStackingRule]
     *
     * @param id The id of the item to check.
     * @return true if the item was can be stacked, false otherwise.
     */
    fun stackable(id: String): Boolean

    /**
     * Checks if the minimum [quantity] has been reached for the item at the specified [index].
     *
     * @param index The index of the item to check.
     * @param quantity The minimum quantity required.
     * @return true if the minimum quantity has been reached, false otherwise.
     */
    fun checkRemoval(index: Int, quantity: Int): Boolean

    /**
     * Sets the [item] at [index] in the current container
     *
     * @param index The index at which to set the item.
     * @param item The item to set.
     * @param moved Whether the item was moved from
     */
    fun set(index: Int, item: Item?, moved: Boolean = false)

    /**
     * Sets the [item] at the specified [index] in the specified [container]
     *
     * @param container The container in which to set the item.
     * @param index The index at which to set the item.
     * @param item The item to set.
     * @param moved Whether the item was moved from another container.
     */
    fun set(container: Container, index: Int, item: Item?, moved: Boolean = false)

    /**
     * Marks the specified [container] as being part of this transaction
     */
    fun mark(container: Container)

    /**
     * Checks if the item [id] and [quantity] are invalid
     *
     * @param id The id of the item to check.
     * @param quantity The amount of the item to check.
     */
    fun invalid(id: String, quantity: Int): Boolean

    /**
     * Checks if the item at [index] is invalid
     *
     * @param index The index of the item to check.
     * @param allowEmpty Whether the index is allowed to be empty
     */
    fun invalid(index: Int, allowEmpty: Boolean = false): Boolean

    /**
     * Checks if the item at [index] in [container] is invalid
     *
     * @param index The index of the item to check.
     * @param container The container in which to check the item.
     * @param allowEmpty Whether the index is allowed to be empty
     */
    fun invalid(container: Container, index: Int, allowEmpty: Boolean = false): Boolean

    /**
     * Sets the [reason] for a transaction error
     *
     * @param reason The reason for the failure of the operation
     */
    fun error(reason: TransactionError) {
        error = reason
    }
}