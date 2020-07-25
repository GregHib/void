package rs.dusk.engine.client.ui

import rs.dusk.engine.model.entity.character.player.Player

class Interfaces(
    private val io: InterfaceIO,
    private val interfaces: InterfacesLookup,
    private val gameFrame: GameFrame,
    private val openInterfaces: MutableSet<Interface> = mutableSetOf()
) {

    fun open(name: String): Boolean = open(interfaces.get(name))

    fun open(id: Int): Boolean = open(interfaces.get(id))

    private fun open(inter: Interface): Boolean {
        if (!hasOpenOrRootParent(inter)) {
            return false
        }
        return sendIfOpened(inter)
    }

    fun close(name: String): Boolean = close(interfaces.get(name))

    fun close(id: Int): Boolean = close(interfaces.get(id))

    private fun close(inter: Interface): Boolean {
        if (removeInterfaceAndChildren(inter)) {
            io.sendClose(inter)
            return true
        }
        return false
    }

    fun closeChildren(name: String): Boolean = closeChildren(interfaces.get(name))

    fun closeChildren(id: Int): Boolean = closeChildren(interfaces.get(id))

    private fun closeChildren(inter: Interface): Boolean {
        if (contains(inter)) {
            removeChildrenOf(inter)
            return true
        }
        return false
    }

    fun remove(name: String): Boolean = remove(interfaces.get(name))

    fun remove(id: Int): Boolean = remove(interfaces.get(id))

    private fun remove(inter: Interface): Boolean {
        if (openInterfaces.remove(inter)) {
            io.sendClose(inter)
            return true
        }
        return false
    }

    fun contains(name: String): Boolean = contains(interfaces.getSafe(name))

    fun contains(id: Int): Boolean = contains(interfaces.get(id))

    private fun contains(inter: Interface): Boolean = openInterfaces.contains(inter)

    fun refresh() {
        openInterfaces.forEach(io::sendOpen)
    }

    private fun hasOpenOrRootParent(inter: Interface): Boolean = parentIsRoot(inter) || hasOpenParent(inter)

    private fun hasOpenParent(inter: Interface): Boolean = contains(inter.getParent(gameFrame.resizable))

    private fun parentIsRoot(inter: Interface): Boolean = inter.getParent(gameFrame.resizable) == ROOT_ID

    private fun sendIfOpened(inter: Interface): Boolean {
        if (openInterfaces.add(inter)) {
            io.sendOpen(inter)
            return true
        }
        return false
    }

    private fun removeInterfaceAndChildren(inter: Interface): Boolean {
        if (openInterfaces.remove(inter)) {
            removeChildrenOf(inter)
            return true
        }
        return false
    }

    private fun removeChildrenOf(parent: Interface) {
        val children = getChildren(parent.id)
        openInterfaces.removeAll(children)
        children.forEach(::removeChildrenOf)
    }

    private fun getChildren(parent: Int): List<Interface> = openInterfaces.filter { it.getParent(gameFrame.resizable) == parent }

    companion object {
        const val ROOT_ID = -1
        const val ROOT_INDEX = 0
    }
}

fun Player.open(interfaceName: String) = interfaces.open(interfaceName)

fun Player.isOpen(interfaceName: String) = interfaces.contains(interfaceName)

fun Player.close(interfaceName: String) = interfaces.close(interfaceName)

fun Player.closeChildren(interfaceName: String) = interfaces.closeChildren(interfaceName)