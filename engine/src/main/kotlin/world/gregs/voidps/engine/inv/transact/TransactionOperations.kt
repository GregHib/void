package world.gregs.voidps.engine.inv.transact

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

fun Transaction.remove(items: List<Item>) {
    for (item in items) {
        remove(item.id, item.amount)
    }
}

@JvmName("removePairs")
fun Transaction.remove(items: List<Pair<String, Int>>) {
    for ((id, amount) in items) {
        remove(id, amount)
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
        return
    }
    discharge(index)
}