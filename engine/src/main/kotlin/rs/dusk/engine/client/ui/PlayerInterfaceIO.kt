package rs.dusk.engine.client.ui

import rs.dusk.engine.client.send
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.InterfaceCloseMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceOpenMessage
import rs.dusk.network.rs.codec.game.encode.message.InterfaceUpdateMessage

class PlayerInterfaceIO(val player: Player) : InterfaceIO {

    override fun sendOpen(inter: Interface) {
        val parent = inter.getParent(player.gameframe.resizable)
        val index = inter.getIndex(player.gameframe.resizable)
        if(parent == -1) {
            player.send(InterfaceUpdateMessage(inter.id, 0))
        } else {
            player.send(InterfaceOpenMessage(true, parent, index, inter.id))
        }
    }

    override fun sendClose(inter: Interface) {
        val index = inter.getIndex(player.gameframe.resizable)
        player.send(InterfaceCloseMessage(inter.id, index))
    }

}