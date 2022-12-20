package world.gregs.voidps.engine.entity.character.contain.transact

import world.gregs.voidps.engine.entity.character.contain.Container

fun Container.replace(id: String, with: String) = transaction { replace(id, with) }

fun Container.replace(index: Int, id: String, with: String) = transaction { replace(index, id, with) }

fun Container.swap(fromIndex: Int, toIndex: Int) = transaction { swap(fromIndex, toIndex) }

fun Container.swap(fromIndex: Int, target: Container, toIndex: Int) = transaction { swap(fromIndex, target, toIndex) }

fun Container.moveAll(target: Container) = transaction { moveAll(target) }

fun Container.move(fromIndex: Int, toIndex: Int) = transaction { move(fromIndex, toIndex) }

fun Container.move(fromIndex: Int, target: Container) = transaction { move(fromIndex, target) }

fun Container.move(fromIndex: Int, target: Container, toIndex: Int) = transaction { move(fromIndex, target, toIndex) }

fun Container.remove(id: String, amount: Int = 1) = transaction { remove(id, amount) }

fun Container.remove(index: Int, id: String, amount: Int = 1) = transaction {
    if (!container.isValidId(index, id)) {
        return@transaction
    }
    remove(id, amount)
}

fun Container.decrement(index: Int, id: String, amount: Int = 1) = transaction {
    val item = container.getItem(index)
    if (item.isEmpty() || item.id != id) {
        error = TransactionError.Invalid
        return@transaction
    }
    if (item.amount < amount) {
        error = TransactionError.Deficient(item.amount)
        return@transaction
    }
    if (container.removalCheck.shouldRemove(index, item.amount - amount)) {
        set(index, null)
    } else {
        set(index, item.copy(amount = item.amount - amount))
    }
}

fun Container.moveToLimit(id: String, amount: Int, target: Container) = transaction { moveToLimit(id, amount, target) }

fun Container.shiftInsert(fromIndex: Int, target: Container, toIndex: Int) = transaction { shiftInsert(fromIndex, target, toIndex) }
