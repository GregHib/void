package world.gregs.voidps.engine.client.ui

import world.gregs.voidps.engine.client.ui.detail.InterfaceComponentDetail
import world.gregs.voidps.engine.client.ui.detail.InterfaceDetail
import world.gregs.voidps.engine.client.ui.event.InterfaceClosed
import world.gregs.voidps.engine.client.ui.event.InterfaceOpened
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.encode.*

/**
 * Instructions to external systems
 */
class PlayerInterfaceIO(
    val player: Player
) : InterfaceIO {

    override fun sendOpen(inter: InterfaceDetail) {
        val parent = inter.getParent(player.gameFrame.resizable)
        if (parent == -1) {
            player.client?.updateInterface(inter.id, 0)
        } else {
            val index = inter.getIndex(player.gameFrame.resizable)
            val permanent = inter.type != "main_screen" && inter.type != "underlay" && inter.type != "dialogue_box"
            player.client?.openInterface(permanent, parent, index, inter.id)
        }
    }

    override fun sendClose(inter: InterfaceDetail) {
        val index = inter.getIndex(player.gameFrame.resizable)
        val parent = inter.getParent(player.gameFrame.resizable)
        player.client?.closeInterface(parent, index)
    }

    override fun notifyClosed(inter: InterfaceDetail) {
        player.events.emit(InterfaceClosed(inter.id, inter.name))
    }

    override fun notifyOpened(inter: InterfaceDetail) {
        player.events.emit(InterfaceOpened(inter.id, inter.name))
    }

    override fun notifyRefreshed(inter: InterfaceDetail) {
        player.events.emit(InterfaceRefreshed(inter.id, inter.name))
    }

    override fun sendPlayerHead(component: InterfaceComponentDetail) {
        player.client?.playerDialogueHead(component.parent, component.id)
    }

    override fun sendAnimation(component: InterfaceComponentDetail, animation: Int) {
        player.client?.animateInterface(component.parent, component.id, animation)
    }

    override fun sendNPCHead(component: InterfaceComponentDetail, npc: Int) {
        player.client?.npcDialogueHead(component.parent, component.id, npc)
    }

    override fun sendText(component: InterfaceComponentDetail, text: String) {
        player.client?.interfaceText(component.parent, component.id, text)
    }

    override fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean) {
        player.client?.interfaceVisibility(component.parent, component.id, !visible)
    }

    override fun sendSprite(component: InterfaceComponentDetail, sprite: Int) {
        player.client?.interfaceSprite(component.parent, component.id, sprite)
    }

    override fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int) {
        player.client?.interfaceItem(component.parent, component.id, item, amount)
    }
}