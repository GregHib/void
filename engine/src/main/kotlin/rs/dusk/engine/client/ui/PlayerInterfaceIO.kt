package rs.dusk.engine.client.ui

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.event.InterfaceClosed
import rs.dusk.engine.client.ui.event.InterfaceOpened
import rs.dusk.engine.client.ui.event.InterfaceRefreshed
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.InterfaceCloseMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceOpenMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceUpdateMessage

class PlayerInterfaceIO(val player: Player, val bus: EventBus) : InterfaceIO {

    override fun sendOpen(inter: Interface) {
        val parent = inter.getParent(player.gameFrame.resizable)
        if(parent == -1) {
            player.send(InterfaceUpdateMessage(inter.id, 0))
        } else {
            val index = inter.getIndex(player.gameFrame.resizable)
            val permanent = inter.type != "main_screen"
            player.send(InterfaceOpenMessage(permanent, parent, index, inter.id))
        }
    }

    override fun sendClose(inter: Interface) {
        val index = inter.getIndex(player.gameFrame.resizable)
        player.send(InterfaceCloseMessage(inter.id, index))
    }

    override fun notifyClosed(inter: Interface) {
        bus.emit(InterfaceClosed(player, inter.id))
    }

    override fun notifyOpened(inter: Interface) {
        bus.emit(InterfaceOpened(player, inter.id))
    }

    override fun notifyRefreshed(inter: Interface) {
        bus.emit(InterfaceRefreshed(player, inter.id))
    }

}