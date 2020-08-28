package rs.dusk.world.community.trade

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage
import rs.dusk.utility.get

object Trade {

    fun sendWarningFlash(player: Player, name: String, component: String, width: Int, height: Int, slot: Int) {
        val details: InterfaceDetails = get()
        val comp = details.getComponentOrNull(name, component) ?: return
        sendWarningFlash(player, comp.parent, comp.id, width, height, slot)
    }

    fun sendWarningFlash(player: Player, id: Int, component: Int, width: Int, height: Int, slot: Int): Boolean {
        player.send(ScriptMessage(143, (id shl 16) or component, width, height, slot))
        return true
    }

    fun sendContainerOptions(player: Player, id: Int, component: Int, container: Int, secondary: Boolean, width: Int, height: Int, options: Array<String>): Boolean {
        player.send(ScriptMessage(if(secondary) 695 else 150, (id shl 16) or component, container, width, height, 0, -1, *options))
        return true
    }
}