package rs.dusk.world.community.trade

import rs.dusk.engine.action.ActionType
import rs.dusk.engine.event.then
import rs.dusk.engine.event.where
import rs.dusk.world.interact.entity.player.display.InterfaceOption

/**
 * Declining or closing cancels the trade
 */

fun isTradeInterface(name: String) = name == "trade_main" || name == "trade_confirm"

fun isDecline(component: String, option: String) = component == "decline" && option == "Decline"

fun isClose(component: String, option: String) = component == "close" && option == "Close"

InterfaceOption where { isTradeInterface(name) && (isDecline(component, option) || isClose(component, option)) } then {
    player.action.cancel(ActionType.Trade)
}