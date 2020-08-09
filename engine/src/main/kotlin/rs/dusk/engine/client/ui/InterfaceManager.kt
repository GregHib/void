package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.player.PlayerGameFrame

/**
 * API for the interacting and tracking of client interfaces
 */
class InterfaceManager(
    private val io: InterfaceIO,
    interfaces: InterfaceDetails,
    private val gameFrame: PlayerGameFrame,
    private val openInterfaces: MutableSet<InterfaceDetail> = mutableSetOf()
) : Interfaces(interfaces) {

    override fun open(inter: InterfaceDetail): Boolean {
        if (!hasOpenOrRootParent(inter)) {
            return false
        }
        return sendIfOpened(inter)
    }

    override fun close(inter: InterfaceDetail): Boolean {
        if (remove(inter)) {
            closeChildrenOf(inter)
            return true
        }
        return false
    }

    override fun closeChildren(inter: InterfaceDetail): Boolean {
        if (contains(inter)) {
            closeChildrenOf(inter)
            return true
        }
        return false
    }

    override fun remove(inter: InterfaceDetail): Boolean {
        if (openInterfaces.remove(inter)) {
            io.sendClose(inter)
            io.notifyClosed(inter)
            return true
        }
        return false
    }

    override fun get(type: String) = openInterfaces.firstOrNull { it.type == type }?.id

    override fun contains(inter: InterfaceDetail): Boolean = openInterfaces.contains(inter)

    override fun refresh() {
        openInterfaces.forEach { inter ->
            io.sendOpen(inter)
            io.notifyRefreshed(inter)
        }
    }

    override fun sendPlayerHead(inter: InterfaceDetail, component: Int): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendPlayerHead(inter, component)
        return true
    }

    override fun sendAnimation(inter: InterfaceDetail, component: Int, animation: Int): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendAnimation(inter, component, animation)
        return true
    }

    override fun sendNPCHead(inter: InterfaceDetail, component: Int, npc: Int): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendNPCHead(inter, component, npc)
        return true
    }

    override fun sendText(inter: InterfaceDetail, component: Int, text: String): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendText(inter, component, text)
        return true
    }

    override fun sendVisibility(inter: InterfaceDetail, component: Int, visible: Boolean): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendVisibility(inter, component, visible)
        return true
    }

    override fun sendSprite(inter: InterfaceDetail, component: Int, sprite: Int): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendSprite(inter, component, sprite)
        return true
    }

    override fun sendItem(inter: InterfaceDetail, component: Int, item: Int, amount: Int): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendItem(inter, component, item, amount)
        return true
    }

    override fun sendSetting(inter: InterfaceDetail, component: Int, from: Int, to: Int, setting: Int): Boolean {
        if(!inter.components.containsKey(component)) {
            return false
        }
        io.sendSettings(inter, component, from, to, setting)
        return true
    }

    private fun hasOpenOrRootParent(inter: InterfaceDetail): Boolean = parentIsRoot(inter) || hasOpenParent(inter)

    private fun parentIsRoot(inter: InterfaceDetail): Boolean = inter.getParent(gameFrame.resizable) == ROOT_ID

    private fun hasOpenParent(inter: InterfaceDetail): Boolean = contains(inter.getParent(gameFrame.resizable))

    private fun sendIfOpened(inter: InterfaceDetail): Boolean {
        if (openInterfaces.add(inter)) {
            io.sendOpen(inter)
            io.notifyOpened(inter)
            return true
        }
        io.notifyRefreshed(inter)
        return false
    }

    private fun closeChildrenOf(parent: InterfaceDetail) {
        val children = getChildren(parent.id)
        children.forEach { child ->
            close(child)
        }
    }

    private fun getChildren(parent: Int): List<InterfaceDetail> =
        openInterfaces.filter { inter -> inter.getParent(gameFrame.resizable) == parent }
}