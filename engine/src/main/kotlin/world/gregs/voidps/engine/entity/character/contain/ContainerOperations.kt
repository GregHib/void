package world.gregs.voidps.engine.entity.character.contain

fun Container.replace(id: String, with: String) = transaction { replace(id, with) }

fun Container.replace(index: Int, id: String, with: String) = transaction { replace(index, id, with) }

fun Container.swap(fromIndex: Int, toIndex: Int) = transaction { swap(fromIndex, toIndex) }

fun Container.swap(fromIndex: Int, target: Container, toIndex: Int) = transaction { swap(fromIndex, target, toIndex) }

fun Container.moveAll(target: Container) = transaction { moveAll(target) }

fun Container.move(fromIndex: Int, toIndex: Int) = transaction { move(fromIndex, toIndex) }

fun Container.move(fromIndex: Int, target: Container) = transaction { move(fromIndex, target) }

fun Container.move(fromIndex: Int, target: Container, toIndex: Int) = transaction { move(fromIndex, target, toIndex) }

fun Container.moveToLimit(id: String, amount: Int, target: Container) = transaction { moveToLimit(id, amount, target) }

fun Container.shiftInsert(fromIndex: Int, target: Container, toIndex: Int) = transaction { shiftInsert(fromIndex, target, toIndex) }

fun Container.add(id: String, amount: Int = 1) = transaction { add(id, amount) }

fun Container.remove(id: String, amount: Int = 1) = transaction { remove(id, amount) }

fun Container.clear(index: Int) = transaction { clear(index) }

fun Container.clear() = transaction { clear() }
