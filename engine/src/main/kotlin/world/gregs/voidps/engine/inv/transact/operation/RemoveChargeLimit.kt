package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveCharge.discharge

/**
 * Transaction operation for removing charges from an item in an inventory.
 * Chargeable items have their charge reduced by the amount required
 * Non-chargeable items are ignored.
 */
object RemoveChargeLimit {

    /**
     * Decreases the charges of an item at [index] until none are remaining or the desired amount is removed.
     * @param index the index of the item in the inventory.
     * @param amount the number of charges to be removed from the item.
     * @return the number of charges actually removed.
     */
    fun TransactionOperation.dischargeToLimit(index: Int, amount: Int): Int {
        if (failed) {
            return 0
        }
        discharge(index, amount)
        return when (val error = error) {
            TransactionError.None -> amount
            is TransactionError.Deficient -> {
                this.error = TransactionError.None
                if (error.amount > 0) {
                    discharge(index, error.amount)
                }
                error.amount
            }
            else -> 0
        }
    }
}
