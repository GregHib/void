package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.community.trade.Trade.getPartner

/**
 * Both players accepting the request moves onto the confirmation screen.
 * Both players accepting the confirmation exchanges items and finishes the trade.
 */

on<InterfaceOption>({ id == "trade_main" && component == "accept" && option == "Accept" }) { player: Player ->
    val partner = getPartner(player) ?: return@on
    if (player.offer.count - partner.offer.count > partner.inventory.spaces) {
        player.message("Other player doesn't have enough inventory space to accept this trade.")
        return@on
    }
    if (partner.offer.count - player.offer.count > player.inventory.spaces) {
        player.message("You don't have enough inventory space to accept this trade.")
        return@on
    }
    player.interfaces.sendText("trade_main", "status", "Waiting for other player...")
    partner.interfaces.sendText("trade_main", "status", "Other player has accepted.")
    player.requests.add(partner, "accept_trade") { requester, acceptor ->
        confirm(requester)
        confirm(acceptor)
    }
}

fun confirm(player: Player) {
    player.interfaces.apply {
        remove("trade_main")
        open("trade_confirm")
    }
    player.interfaces.sendText("trade_confirm", "status", "Are you sure you want to make this trade?")
}

on<InterfaceOption>({ id == "trade_confirm" && component == "accept" && option == "Accept" }) { player: Player ->
    val partner = getPartner(player) ?: return@on
    player.interfaces.sendText("trade_confirm", "status", "Waiting for other player...")
    partner.interfaces.sendText("trade_confirm", "status", "Other player has accepted.")
    player.requests.add(partner, "confirm_trade") { requester, acceptor ->
        requester.action.resume()
        acceptor.action.resume()
    }
}
