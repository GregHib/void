package rs.dusk.engine.client.ui

class InterfaceManager(
    private val io: InterfaceIO,
    interfaces: InterfacesLookup,
    private val gameFrame: GameFrame,
    private val openInterfaces: MutableSet<Interface> = mutableSetOf()
) : Interfaces(interfaces) {

    override fun open(inter: Interface): Boolean {
        if (!hasOpenOrRootParent(inter)) {
            return false
        }
        return sendIfOpened(inter)
    }

    override fun close(inter: Interface): Boolean {
        if (remove(inter)) {
            closeChildrenOf(inter)
            return true
        }
        return false
    }

    override fun closeChildren(inter: Interface): Boolean {
        if (contains(inter)) {
            closeChildrenOf(inter)
            return true
        }
        return false
    }

    override fun remove(inter: Interface): Boolean {
        if (openInterfaces.remove(inter)) {
            io.sendClose(inter)
            io.notifyClosed(inter)
            return true
        }
        return false
    }

    override fun get(type: String) = openInterfaces.firstOrNull { it.type == type }?.id

    override fun contains(inter: Interface): Boolean = openInterfaces.contains(inter)

    override fun refresh() {
        openInterfaces.forEach { inter ->
            io.sendOpen(inter)
            io.notifyRefreshed(inter)
        }
    }

    private fun hasOpenOrRootParent(inter: Interface): Boolean = parentIsRoot(inter) || hasOpenParent(inter)

    private fun parentIsRoot(inter: Interface): Boolean = inter.getParent(gameFrame.resizable) == ROOT_ID

    private fun hasOpenParent(inter: Interface): Boolean = contains(inter.getParent(gameFrame.resizable))

    private fun sendIfOpened(inter: Interface): Boolean {
        if (openInterfaces.add(inter)) {
            io.sendOpen(inter)
            io.notifyOpened(inter)
            return true
        }
        io.notifyRefreshed(inter)
        return false
    }

    private fun closeChildrenOf(parent: Interface) {
        val children = getChildren(parent.id)
        children.forEach { child ->
            close(child)
        }
    }

    private fun getChildren(parent: Int): List<Interface> =
        openInterfaces.filter { inter -> inter.getParent(gameFrame.resizable) == parent }
}