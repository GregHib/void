package rs.dusk.engine.client.ui

import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
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

    override fun get(type: String): String? {
        return openInterfaces.firstOrNull { it.type == type }?.name
    }

    override fun contains(inter: InterfaceDetail): Boolean = openInterfaces.contains(inter)

    override fun refresh() {
        openInterfaces.forEach { inter ->
            io.sendOpen(inter)
            io.notifyRefreshed(inter)
        }
    }

    override fun sendPlayerHead(component: InterfaceComponentDetail): Boolean {
        io.sendPlayerHead(component)
        return true
    }

    override fun sendAnimation(component: InterfaceComponentDetail, animation: Int): Boolean {
        io.sendAnimation(component, animation)
        return true
    }

    override fun sendNPCHead(component: InterfaceComponentDetail, npc: Int): Boolean {
        io.sendNPCHead(component, npc)
        return true
    }

    override fun sendText(component: InterfaceComponentDetail, text: String): Boolean {
        io.sendText(component, text)
        return true
    }

    override fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean): Boolean {
        io.sendVisibility(component, visible)
        return true
    }

    override fun sendSprite(component: InterfaceComponentDetail, sprite: Int): Boolean {
        io.sendSprite(component, sprite)
        return true
    }

    override fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int): Boolean {
        io.sendItem(component, item, amount)
        return true
    }

    override fun sendSetting(component: InterfaceComponentDetail, from: Int, to: Int, setting: Int): Boolean {
        io.sendSettings(component, from, to, setting)
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