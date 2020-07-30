package rs.dusk.engine.client.ui

import rs.dusk.engine.action.Suspension
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.utility.get

abstract class Interfaces(private val interfaces: InterfacesLookup) {

    fun open(name: String): Boolean = open(interfaces.get(name))

    fun open(id: Int): Boolean = open(interfaces.get(id))

    protected abstract fun open(inter: Interface): Boolean

    fun close(name: String): Boolean = close(interfaces.get(name))

    fun close(id: Int): Boolean = close(interfaces.get(id))

    protected abstract fun close(inter: Interface): Boolean

    abstract fun get(type: String): Int?

    fun closeChildren(name: String): Boolean = closeChildren(interfaces.get(name))

    fun closeChildren(id: Int): Boolean = closeChildren(interfaces.get(id))

    protected abstract fun closeChildren(inter: Interface): Boolean

    fun remove(name: String): Boolean = remove(interfaces.get(name))

    fun remove(id: Int): Boolean = remove(interfaces.get(id))

    protected abstract fun remove(inter: Interface): Boolean

    fun contains(name: String): Boolean = contains(interfaces.getSafe(name))

    fun contains(id: Int): Boolean = contains(interfaces.get(id))

    protected abstract fun contains(inter: Interface): Boolean

    abstract fun refresh()

    companion object {
        const val ROOT_ID = -1
        const val ROOT_INDEX = 0
    }
}

fun Player.open(interfaceName: String): Boolean {
    val lookup: InterfacesLookup = get()
    val inter = lookup.get(interfaceName)
    if (inter.type.isNotEmpty()) {
        val id = interfaces.get(inter.type)
        if (id != null) {
            interfaces.close(id)
        }
    }
    return interfaces.open(interfaceName)
}

fun Player.isOpen(interfaceName: String) = interfaces.contains(interfaceName)

fun Player.hasOpen(interfaceType: String) = interfaces.get(interfaceType) != null

fun Player.close(interfaceName: String) = interfaces.close(interfaceName)

fun Player.closeType(interfaceType: String): Boolean {
    val id = interfaces.get(interfaceType) ?: return false
    return interfaces.close(id)
}

fun Player.closeChildren(interfaceName: String) = interfaces.closeChildren(interfaceName)

suspend fun Player.awaitInterfaces(): Boolean {
    val id = interfaces.get("main_screen")
    if (id != null) {
        action.await<Unit>(Suspension.Interface(id))
    }
    return true
}