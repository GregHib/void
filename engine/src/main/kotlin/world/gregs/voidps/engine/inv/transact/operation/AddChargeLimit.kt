package world.gregs.voidps.engine.inv.transact.operation

import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddCharge.charge

/**
 * Transaction operation for adding as many items as possible from an inventory.
 * Adds items to the inventory until it reaches its capacity or the desired amount is added.
 */
object AddChargeLimit {

    /**
     * Increases the charges of the item at the specified index until it reaches its capacity or the desired amount is added.
     * @param index the index of the item to be charged.
     * @param amount the number of charges to be added.
     * @return the number of charges actually added.
     */
    fun TransactionOperation.chargeToLimit(index: Int, amount: Int): Int {
        if (failed) {
            return 0
        }
        charge(index, amount)
        return when (val error = error) {
            TransactionError.None -> amount
            is TransactionError.Full -> {
                this.error = TransactionError.None
                if (error.amount > 0) {
                    charge(index, error.amount)
                }
                error.amount
            }
            else -> 0
        }
    }
}
