package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.transact.charge
import world.gregs.voidps.engine.inv.transact.discharge
import world.gregs.voidps.engine.inv.transact.clearCharges
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.inv.transact.operation.SwapItem.swap
import world.gregs.voidps.engine.inv.transact.operation.MoveItem.move
import world.gregs.voidps.engine.inv.transact.operation.MoveItem.moveAll
import world.gregs.voidps.engine.inv.transact.operation.ShiftItem.shift
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ClearItem.clear

fun Inventory.replace(id: String, with: String) = transaction { replace(id, with) }

fun Inventory.replace(index: Int, id: String, with: String) = transaction { replace(index, id, with) }

fun Inventory.swap(fromIndex: Int, toIndex: Int) = transaction { swap(fromIndex, toIndex) }

fun Inventory.swap(fromIndex: Int, target: Inventory, toIndex: Int) = transaction { swap(fromIndex, target, toIndex) }

fun Inventory.moveAll(target: Inventory) = transaction { moveAll(target) }

fun Inventory.move(fromIndex: Int, toIndex: Int) = transaction { move(fromIndex, toIndex) }

fun Inventory.move(fromIndex: Int, target: Inventory) = transaction { move(fromIndex, target) }

fun Inventory.move(fromIndex: Int, target: Inventory, toIndex: Int) = transaction { move(fromIndex, target, toIndex) }

fun Inventory.moveToLimit(id: String, amount: Int, target: Inventory): Int {
    var moved = 0
    transaction {
        moved = moveToLimit(id, amount, target)
    }
    return moved
}

fun Inventory.shift(fromIndex: Int, toIndex: Int) = transaction { shift(fromIndex, toIndex) }

fun Inventory.add(id: String, amount: Int = 1) = transaction { add(id, amount) }

fun Inventory.remove(id: String, amount: Int = 1) = transaction { remove(id, amount) }

fun Inventory.remove(index: Int, id: String, amount: Int = 1) = transaction { remove(index, id, amount) }

fun Inventory.removeToLimit(id: String, amount: Int = 1): Int {
    var removed = 0
    transaction {
        removed = removeToLimit(id, amount)
    }
    return removed
}

fun Inventory.clear(index: Int) = transaction { clear(index) }

fun Inventory.clear() = transaction { clear() }

fun Inventory.contains(vararg pairs: Pair<String, Int>) = pairs.all { (id, amount) -> contains(id, amount) }

fun Inventory.contains(pairs: List<Pair<String, Int>>) = pairs.all { (id, amount) -> contains(id, amount) }

fun Inventory.charge(player: Player, index: Int, amount: Int = 1) = transaction { charge(player, index, amount) }

fun Inventory.discharge(player: Player, index: Int, amount: Int = 1) = transaction { discharge(player, index, amount) }

fun Inventory.clearCharges(player: Player, index: Int) = transaction { clearCharges(player, index) }

fun Inventory.charges(player: Player, index: Int): Int {
    val item = getOrNull(index) ?: return 0
    if (item.isEmpty()) {
        return 0
    }
    val variable: String? = item.def.getOrNull("charge")
    if (variable != null) {
        return player[variable, 0]
    }
    return item.charges
}