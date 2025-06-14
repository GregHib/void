package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

/**
 * Transaction operation for moving an item inside an inventory.
 * The moveToLimit operation moves items from the current inventory to another inventory until
 * the target inventory reaches its capacity or the desired amount is moved.
 */
object MoveItemLimit {

    /**
     * Moves items from the current inventory to another inventory until the target
     * inventory reaches its capacity or the desired amount is moved.
     * @param id the identifier of the item to be moved.
     * @param amount the number of items to be moved.
     * @param target the target inventory for the items.
     * @return the number of items actually moved.
     */
    fun TransactionOperation.moveToLimit(id: String, amount: Int, target: Inventory, replace: String = id): Int {
        if (failed) {
            return 0
        }

        val transaction = link(target)
        val added = transaction.addToLimit(replace, amount)
        if (added == 0) {
            return 0
        }
        val removed = removeToLimit(id, added)
        if (failed) {
            return 0
        }
        if (removed < added) {
            // Undo and redo changes to target so items are in the correct place
            transaction.remove(replace, added)
            transaction.changes.clear()
            if (removed > 0) {
                transaction.add(replace, removed)
            }
        }
        return removed
    }

    /**
     * Moves as many items from the current inventory to another inventory until the target
     * inventory reaches its capacity or everything is moved.
     * @param target the target inventory for the items.
     */
    fun TransactionOperation.moveAllToLimit(target: Inventory) {
        if (failed) {
            return
        }
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isEmpty()) {
                continue
            }
            moveToLimit(item.id, item.amount, target)
        }
    }
}
