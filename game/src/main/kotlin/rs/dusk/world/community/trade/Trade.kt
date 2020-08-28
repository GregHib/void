package rs.dusk.world.community.trade

import rs.dusk.engine.client.send
import rs.dusk.engine.client.ui.detail.InterfaceDetails
import rs.dusk.engine.entity.character.contain.Container
import rs.dusk.engine.entity.character.contain.container
import rs.dusk.engine.entity.character.contain.detail.ContainerDetails
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.network.rs.codec.game.encode.message.ScriptMessage
import rs.dusk.utility.get

object Trade {

    fun status(player: Player, status: String) {
        player.interfaces.sendText("trade_main", "status", status)
    }
}

fun Player.warn(name: String, component: String, slot: Int) {
    val details: InterfaceDetails = get()
    val containerDetails: ContainerDetails = get()
    val comp = details.getComponent(name, component)
    val container = containerDetails.get(comp.container)
    send(ScriptMessage(143, (comp.parent shl 16) or comp.id, container.width, container.height, slot))
}

val Player.offer: Container
    get() = container("trade_offer", false)

val Player.otherOffer: Container
    get() = container("trade_offer", true)

val Player.loan: Container
    get() = container("item_loan", false)

val Player.otherLoan: Container
    get() = container("item_loan", true)