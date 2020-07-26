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
        if (removeInterfaceAndChildren(inter)) {
            io.sendClose(inter)
            return true
        }
        return false
    }

    override fun get(type: String) = openInterfaces.firstOrNull { it.type == type }?.id

    override fun closeChildren(inter: Interface): Boolean {
        if (contains(inter)) {
            removeChildrenOf(inter)
            return true
        }
        return false
    }

    override fun remove(inter: Interface): Boolean {
        if (openInterfaces.remove(inter)) {
            io.sendClose(inter)
            return true
        }
        return false
    }

    override fun contains(inter: Interface): Boolean = openInterfaces.contains(inter)

    override fun refresh() {
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
}