package world.gregs.voidps.engine.contain

fun Container.replace(id: String, with: String) = transaction { replace(id, with) }

fun Container.replace(index: Int, id: String, with: String) = transaction { replace(index, id, with) }

fun Container.swap(fromIndex: Int, toIndex: Int) = transaction { swap(fromIndex, toIndex) }

fun Container.swap(fromIndex: Int, target: Container, toIndex: Int) = transaction { swap(fromIndex, target, toIndex) }

fun Container.moveAll(target: Container) = transaction { moveAll(target) }

fun Container.move(fromIndex: Int, toIndex: Int) = transaction { move(fromIndex, toIndex) }

fun Container.move(fromIndex: Int, target: Container) = transaction { move(fromIndex, target) }

fun Container.move(fromIndex: Int, target: Container, toIndex: Int) = transaction { move(fromIndex, target, toIndex) }

fun Container.moveToLimit(id: String, amount: Int, target: Container): Int {
    var moved = 0
    transaction {
        moved = moveToLimit(id, amount, target)
    }
    return moved
}

fun Container.shift(fromIndex: Int, toIndex: Int) = transaction { shift(fromIndex, toIndex) }

fun Container.add(id: String, amount: Int = 1) = transaction { add(id, amount) }

fun Container.remove(id: String, amount: Int = 1) = transaction { remove(id, amount) }

fun Container.removeToLimit(id: String, amount: Int = 1): Int {
    var removed = 0
    transaction {
        removed = removeToLimit(id, amount)
    }
    return removed
}

fun Container.clear(index: Int) = transaction { clear(index) }

fun Container.clear() = transaction { clear() }

fun Container.contains(vararg pairs: Pair<String, Int>) = pairs.all { (id, amount) -> contains(id, amount) }