package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.transact.operation.RemoveCharge.discharge
import world.gregs.voidps.engine.inv.transact.operation.AddCharge.charge
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearCharge.discharge
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

fun Transaction.remove(items: List<Item>) {
    for (item in items) {
        remove(item.id, item.amount)
    }
}

fun Transaction.add(items: List<Item>) {
    for (item in items) {
        add(item.id, item.amount)
    }
}

fun Transaction.charge(player: Player, index: Int, amount: Int) {
    val item = inventory.getOrNull(index)
    if (failed || item == null || item.isEmpty()) {
        error = TransactionError.Invalid
        return
    }
    val variable: String? = item.def.getOrNull("charge")
    if (variable != null) {
        val maximum = item.def["charges", 0]
        player[variable] = (player[variable, 0] + amount).coerceAtMost(maximum)
        return
    }
    charge(index, amount)
}

fun Transaction.discharge(player: Player, index: Int, amount: Int) {
    val item = inventory.getOrNull(index)
    if (failed || item == null || item.isEmpty()) {
        error = TransactionError.Invalid
        return
    }
    val variable: String? = item.def.getOrNull("charge")
    if (variable != null) {
        player[variable] = (player[variable, 0] - amount).coerceAtLeast(0)
        return
    }
    discharge(index, amount)
}

fun Transaction.clearCharges(player: Player, index: Int) {
    val item = inventory.getOrNull(index)
    if (failed || item == null || item.isEmpty()) {
        error = TransactionError.Invalid
        return
    }
    val variable: String? = item.def.getOrNull("charge")
    if (variable != null) {
        player.clear(variable)
        state
        return
    }
    discharge(index)
}

fun Transaction.charges(player: Player, index: Int): Int {
    return inventory.charges(player, index)
}