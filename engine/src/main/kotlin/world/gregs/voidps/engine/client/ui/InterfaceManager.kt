package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetail
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetails
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerGameFrame
import world.gregs.voidps.network.encode.closeInterface
import world.gregs.voidps.network.encode.openInterface
import world.gregs.voidps.network.encode.updateInterface

/**
 * API for the interacting and tracking of client interfaces
 */
class InterfaceManager(
    private val player: Player,
    private val io: InterfaceIO,
    interfaces: InterfaceDetails,
    private val gameFrame: PlayerGameFrame,
    private val openInterfaces: MutableSet<String> = mutableSetOf()
) : Interfaces(interfaces) {

    override fun open(name: String): Boolean {
        if (!hasOpenOrRootParent(name)) {
            return false
        }
        return sendIfOpened(name)
    }

    override fun close(name: String): Boolean {
        if (remove(name)) {
            closeChildrenOf(name)
            return true
        }
        return false
    }

    override fun closeChildren(name: String): Boolean {
        if (contains(name)) {
            closeChildrenOf(name)
            return true
        }
        return false
    }

    override fun remove(name: String): Boolean {
        if (openInterfaces.remove(name)) {
            val inter = details.get(name)
            sendClose(inter)
            notifyClosed(inter)
            return true
        }
        return false
    }

    override fun get(type: String): String? {
        return openInterfaces.firstOrNull { details.get(it).type == type }
    }

    override fun contains(name: String): Boolean = openInterfaces.contains(name)

    override fun refresh() {
        openInterfaces.forEach {
            val inter = details.get(it)
            sendOpen(inter)
            notifyRefreshed(inter)
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

    private fun hasOpenOrRootParent(name: String): Boolean {
        val parent = details.get(name).getParent(gameFrame.resizable)
        return parent == ROOT_ID || contains(parent)
    }

    private fun sendIfOpened(name: String): Boolean {
        if (openInterfaces.add(name)) {
            val inter = details.get(name)
            sendOpen(inter)
            notifyOpened(inter)
            return true
        }
        val inter = details.get(name)
        notifyRefreshed(inter)
        return false
    }

    private fun closeChildrenOf(parent: String) {
        getChildren(parent).forEach(::close)
    }

    private fun getChildren(parent: String): List<String> =
        openInterfaces.filter { name -> details.get(name).getParent(gameFrame.resizable) == parent }

    private fun sendOpen(inter: InterfaceDetail) {
        val parent = inter.getParent(player.gameFrame.resizable)
        if (parent == ROOT_ID) {
            player.client?.updateInterface(inter.id, 0)
        } else {
            val index = inter.getIndex(player.gameFrame.resizable)
            val permanent = inter.type != "main_screen" && inter.type != "underlay" && inter.type != "dialogue_box"
            player.client?.openInterface(permanent, details.get(parent).id, index, inter.id)
        }
    }

    private fun sendClose(inter: InterfaceDetail) {
        val index = inter.getIndex(player.gameFrame.resizable)
        val parent = inter.getParent(player.gameFrame.resizable)
        player.client?.closeInterface(details.get(parent).id, index)
    }

    private fun notifyClosed(inter: InterfaceDetail) {
        player.events.emit(InterfaceClosed(inter.id, inter.name))
    }

    private fun notifyOpened(inter: InterfaceDetail) {
        player.events.emit(InterfaceOpened(inter.id, inter.name))
    }

    private fun notifyRefreshed(inter: InterfaceDetail) {
        player.events.emit(InterfaceRefreshed(inter.id, inter.name))
    }
}