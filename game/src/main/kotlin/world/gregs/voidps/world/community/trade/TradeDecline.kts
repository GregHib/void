package world.gregs.voidps.world.community.trade

import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.event.where
import world.gregs.voidps.world.interact.entity.player.display.InterfaceOption

/**
 * Declining or closing cancels the trade
 */

fun isTradeInterface(name: String) = name == "trade_main" || name == "trade_confirm"

fun isDecline(component: String, option: String) = component == "decline" && option == "Decline"

fun isClose(component: String, option: String) = component == "close" && option == "Close"

InterfaceOption where { isTradeInterface(name) && (isDecline(component, option) || isClose(component, option)) } then {
    player.action.cancel(ActionType.Trade)
}