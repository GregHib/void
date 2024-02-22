package world.gregs.voidps.engine.inv

import world.gregs.voidps.engine.entity.item.Item

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

fun Inventory.addAll(vararg items: Item) = transaction { items.forEach { item -> add(item.id, item.amount) } }

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