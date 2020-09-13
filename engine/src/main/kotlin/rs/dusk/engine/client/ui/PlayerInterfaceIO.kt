package rs.dusk.engine.client.ui

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceComponentDetail
import rs.dusk.engine.client.ui.detail.InterfaceDetail
import rs.dusk.engine.client.ui.event.InterfaceClosed
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.event.InterfaceRefreshed
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.EventBus
import rs.dusk.network.rs.codec.game.encode.message.*

/**
 * Instructions to external systems
 */
class PlayerInterfaceIO(val player: Player, val bus: EventBus) : InterfaceIO {

    override fun sendOpen(inter: InterfaceDetail) {
        val parent = inter.getParent(player.gameFrame.resizable)
        if(parent == -1) {
            player.send(InterfaceUpdateMessage(inter.id, 0))
        } else {
            val index = inter.getIndex(player.gameFrame.resizable)
            val permanent = inter.type != "main_screen" && inter.type != "dialogue_box"
            player.send(InterfaceOpenMessage(permanent, parent, index, inter.id))
        }
    }

    override fun sendClose(inter: InterfaceDetail) {
        val index = inter.getIndex(player.gameFrame.resizable)
        val parent = inter.getParent(player.gameFrame.resizable)
        player.send(InterfaceCloseMessage(parent, index))
    }

    override fun notifyClosed(inter: InterfaceDetail) {
        bus.emit(InterfaceClosed(player, inter.id, inter.name))
    }

    override fun notifyOpened(inter: InterfaceDetail) {
        bus.emit(InterfaceOpened(player, inter.id, inter.name))
    }

    override fun notifyRefreshed(inter: InterfaceDetail) {
        bus.emit(InterfaceRefreshed(player, inter.id, inter.name))
    }

    override fun sendPlayerHead(component: InterfaceComponentDetail) {
        player.send(InterfaceHeadPlayerMessage(component.parent, component.id))
    }

    override fun sendAnimation(component: InterfaceComponentDetail, animation: Int) {
        player.send(InterfaceAnimationMessage(component.parent, component.id, animation))
    }

    override fun sendNPCHead(component: InterfaceComponentDetail, npc: Int) {
        player.send(InterfaceHeadNPCMessage(component.parent, component.id, npc))
    }

    override fun sendText(component: InterfaceComponentDetail, text: String) {
        player.send(InterfaceTextMessage(component.parent, component.id, text))
    }

    override fun sendVisibility(component: InterfaceComponentDetail, visible: Boolean) {
        player.send(InterfaceVisibilityMessage(component.parent, component.id, !visible))
    }

    override fun sendSprite(component: InterfaceComponentDetail, sprite: Int) {
        player.send(InterfaceSpriteMessage(component.parent, component.id, sprite))
    }

    override fun sendItem(component: InterfaceComponentDetail, item: Int, amount: Int) {
        player.send(InterfaceItemMessage(component.parent, component.id, item, amount))
    }
}