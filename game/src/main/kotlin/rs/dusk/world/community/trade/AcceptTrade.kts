import rs.dusk.engine.action.ActionType
import rs.dusk.engine.entity.character.get
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.community.trade.Trade.status
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * Requesting to trade with another player, accepting the request and setting up the trade
 */

InterfaceOption where { name == "trade_main" && component == "accept" && option == "Accept" } then {
    val partner: Player? = player["trade_partner"]
    if(partner == null) {
        player.action.cancel(ActionType.Trade)
        return@then
    }
    status(player, "Waiting for other player...")
    status(partner, "Other player has accepted.")
    player.requests.add(partner, "trade_accept") { requester, acceptor ->
    }
}

InterfaceOption where { name == "trade_main" && component == "decline" && option == "Decline" } then {
    player.action.cancel(ActionType.Trade)
}