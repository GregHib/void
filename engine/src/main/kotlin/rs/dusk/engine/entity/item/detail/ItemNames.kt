package rs.dusk.engine.entity.item.detail

import rs.dusk.engine.entity.definition.ItemDefinitions
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.utility.get

fun Container.stackable(name: String): Boolean {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return false
    return stackable(id)
}

fun Container.contains(name: String): Boolean {
    return indexOf(name) != -1
}

fun Container.indexOf(name: String): Int {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return -1
    return indexOf(id)
}

fun Container.set(index: Int, name: String, amount: Int = 1, update: Boolean = true): Boolean {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return false
    return set(index, id, amount, update)
}

fun Container.replace(name: String, replacement: String): Boolean {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return false
    val replacementId = definitions.getIdOrNull(replacement) ?: return false
    return replace(id, replacementId)
}

fun Container.add(index: Int, name: String, amount: Int = 1): Boolean {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return false
    return add(index, id, amount)
}

fun Container.add(name: String, amount: Int = 1): Boolean {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return false
    return add(id, amount)
}

fun Container.remove(index: Int, name: String, amount: Int = 1): Boolean {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return false
    return remove(index, id, amount)
}

fun Container.remove(name: String, amount: Int = 1): Boolean {
    val definitions: ItemDefinitions = get()
    val id = definitions.getIdOrNull(name) ?: return false
    return remove(id, amount)
}