package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.set
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.world.community.trade.Trade.getPartner
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

/**
 * Both players accepting the request moves onto the confirmation screen.
 * Both players accepting the confirmation exchanges items and finishes the trade.
 */

InterfaceOption where { name == "trade_main" && component == "accept" && option == "Accept" } then {
    val partner = getPartner(player) ?: return@then
    player.interfaces.sendText("trade_main", "status", "Waiting for other player...")
    partner.interfaces.sendText("trade_main", "status", "Other player has accepted.")
    player["accepted_trade"] = true
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

InterfaceOption where { name == "trade_confirm" && component == "accept" && option == "Accept" } then {
    val partner = getPartner(player) ?: return@then
    player.interfaces.sendText("trade_confirm", "status", "Waiting for other player...")
    partner.interfaces.sendText("trade_confirm", "status", "Other player has accepted.")
    player.requests.add(partner, "confirm_trade") { requester, acceptor ->
        requester.action.resume()
        acceptor.action.resume()
    }
}
