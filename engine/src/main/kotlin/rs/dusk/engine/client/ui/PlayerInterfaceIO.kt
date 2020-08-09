package rs.dusk.engine.client.ui

import rs.dusk.engine.client.send
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

    override fun sendPlayerHead(inter: InterfaceDetail, component: Int) {
        player.send(InterfaceHeadPlayerMessage(inter.id, component))
    }

    override fun sendAnimation(inter: InterfaceDetail, component: Int, animation: Int) {
        player.send(InterfaceAnimationMessage(inter.id, component, animation))
    }

    override fun sendNPCHead(inter: InterfaceDetail, component: Int, npc: Int) {
        player.send(InterfaceHeadNPCMessage(inter.id, component, npc))
    }

    override fun sendText(inter: InterfaceDetail, component: Int, text: String) {
        player.send(InterfaceTextMessage(inter.id, component, text))
    }

    override fun sendVisibility(inter: InterfaceDetail, component: Int, visible: Boolean) {
        player.send(InterfaceVisibilityMessage(inter.id, component, !visible))
    }

    override fun sendSprite(inter: InterfaceDetail, component: Int, sprite: Int) {
        player.send(InterfaceSpriteMessage(inter.id, component, sprite))
    }

    override fun sendItem(inter: InterfaceDetail, component: Int, item: Int, amount: Int) {
        player.send(InterfaceItemMessage(inter.id, component, item, amount))
    }

    override fun sendSettings(inter: InterfaceDetail, component: Int, from: Int, to: Int, setting: Int) {
        player.send(InterfaceSettingsMessage(inter.id, component, from, to, setting))
    }
}