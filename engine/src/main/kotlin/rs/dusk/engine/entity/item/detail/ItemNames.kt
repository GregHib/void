package rs.dusk.engine.entity.item.detail

import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.ContainerResult
import rs.dusk.utility.get

fun Container.stackable(name: String): Boolean {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return false
    return stackable(id)
}

fun Container.indexOf(name: String): Int {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return -1
    return indexOf(id)
}

fun Container.set(index: Int, name: String, amount: Int = 1, update: Boolean = true): Boolean {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return false
    return set(index, id, amount, update)
}

fun Container.replace(name: String, replacement: String): Boolean {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return false
    val replacementId = details.getIdOrNull(replacement) ?: return false
    return replace(id, replacementId)
}

fun Container.add(index: Int, name: String, amount: Int = 1): ContainerResult.Addition {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return ContainerResult.Addition.Failure.Invalid
    return add(index, id, amount)
}

fun Container.add(name: String, amount: Int = 1): ContainerResult.Addition {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return ContainerResult.Addition.Failure.Invalid
    return add(id, amount)
}

fun Container.remove(index: Int, name: String, amount: Int = 1): ContainerResult.Removal {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return ContainerResult.Removal.Failure.Invalid
    return remove(index, id, amount)
}

fun Container.remove(name: String, amount: Int = 1): ContainerResult.Removal {
    val details: ItemDetails = get()
    val id = details.getIdOrNull(name) ?: return ContainerResult.Removal.Failure.Invalid
    return remove(id, amount)
}